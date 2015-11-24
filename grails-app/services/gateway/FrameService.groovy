package gateway

import java.sql.Array
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat;
import java.util.Date
import java.util.Hashtable
import java.util.Map
import groovy.sql.Sql
import org.apache.commons.codec.binary.Base64
import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager
import org.postgresql.copy.CopyIn
import bayeos.frame.FrameParser
import bayeos.frame.DefaultFrameHandler
import bayeos.frame.FrameParserException
import gateway.FrameService.BoardRecord;
import gateway.time.ThisMonth;


class FrameService {

	def dataSource

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS+01")

	class BoardRecord {
		Integer id // board id
		Date lrt // 
		Hashtable<Integer,Integer> channels = new Hashtable<>(10) 	// nr, id
		Hashtable<Integer,Date> lrts = new Hashtable<>(10)  // id, lrt
	}


	private Integer findOrSaveBoard(String origin, Boolean frameStorage){
		log.debug("findOrSaveBoard:${origin}")
		def db = new Sql(dataSource)
		try {
			def b = db.firstRow("select id from board where origin like ?;",[origin])
			if (b==null) {
				log.info("Creating new board:${origin}")							
				def seq = db.firstRow("select nextval('board_id_seq') as id;")
				db.execute """ insert into board (id, origin, frame_storage) values (${seq.id},${origin},${frameStorage});"""				
				return seq.id
			} else {
				return b.id
			}
		} catch (SQLException e){
			log.error(e.getMessage())
		} finally {
			db.close()
		}

	}

	private Integer findOrSaveChannel(Integer boardId, Integer channelNr){
		log.debug("findOrSaveChannel: Board id:${boardId} , Channel nr:${channelNr}")
		def db = new Sql(dataSource)
		try {
			def c = db.firstRow("select id from channel where board_id = ? and nr = ?;",[boardId, channelNr])
			if (c==null){
				log.info("Creating new channel:${channelNr} for board:${boardId}")
				def seq = db.firstRow("select nextval('channel_id_seq') as id;")
				db.execute 	"""insert into channel (id, board_id, nr) values (${seq.id},${boardId},${channelNr});"""
				return seq.id
			} else {
				return c.id
			}
		} catch (SQLException e){
			log.error(e.getMessage())
		} finally {
			db.close()
		}
	}
	
	private void parseFrames(FrameParser parser, String sender, String[] frames) {
		if (frames == null)	return
			log.info("Parsing ${frames.length} frames from: ${sender}")
		for(String f:frames) {
			try {
				if (!Base64.isBase64(f)) {
					log.warn("Invalid base64 character in frame:${f}")
				} else {
					parser.parse(Base64.decodeBase64(f))
				}
			} catch (FrameParserException e){
				log.error("FrameParserError: frame:${f}")
			}
		}
	}


	private void updateLrt(Hashtable<String,BoardRecord> boardRecords){
		def db = new Sql(dataSource)
		for (Map.Entry<String, BoardRecord> b : boardRecords.entrySet()) {
			BoardRecord br = b.getValue()
			for(Map.Entry<Integer,Date> c: br.lrts.entrySet()){
				Integer id = c.getKey()
				Date date = c.getValue()
				db.executeUpdate("update channel set last_result_time = ? where id = ?",[new Timestamp(date.getTime()), id])
			}
			if (br.lrt != null){
				db.executeUpdate("update board set last_result_time = ? where id = ?",[new Timestamp(br.lrt.getTime()), br.id])
			}
		}
		db.close()
	}


	def saveFrames(String sender, String[] frames) {
		if ((frames == null) || (sender == null)) return
		Connection con = null
		CopyIn cin = null
		Hashtable<String, BoardRecord> boardRecords = new Hashtable<>()
		long dataFrames = 0
			
		try {
			con = dataSource.getConnection()			
			
			DefaultFrameHandler flatHandler = new DefaultFrameHandler(sender){
				
						private void startCopy(){
							if (cin == null){
							 log.debug("startCopy")
							 CopyManager cm = ((PGConnection)con.unwrap(PGConnection.class)).getCopyAPI()
							 cin = cm.copyIn("COPY observation (channel_id,result_time,result_value) FROM STDIN WITH CSV")
							}
						}		
				
						private void endCopy(){
							if (cin != null){
								log.debug("startEnd")
								long r = cin.endCopy()
								cin = null
							}
						}
			
						
						void onNewOrigin(String origin) {
							log.debug("On new board:${origin}")
							endCopy()
							BoardRecord bc = boardRecords.get(origin)
							if (bc == null){
								Integer id = FrameService.this.findOrSaveBoard(origin)
								boardRecords.put(origin, new BoardRecord(id:id))
							}
						}

						void onNewChannels(String origin, SortedSet<Integer> channels) {
							log.debug("On new channels:${channels} for board:${origin}")
							endCopy()
							BoardRecord bc = boardRecords.get(origin)
							assert(bc!=null)
							channels.removeAll(bc.channels.keySet())

							for (Integer nr:channels){
								Integer id = FrameService.this.findOrSaveChannel(bc.id,nr)
								bc.channels.put(nr,id)
							}
						}

						void onDataFrame(String origin, Date timeStamp, Hashtable<Integer,Float> values, Integer rssi) {
							//log.debug("On dataFrame:${values} for board:${origin}")
							startCopy()
							BoardRecord bc = boardRecords.get(origin)
							assert(bc!=null)

							for (Map.Entry<Integer, Float> item : values.entrySet()) {
								Integer nr  = item.getKey()
								Float value  = item.getValue()
								Integer id = bc.channels.get(nr)
						
								StringBuilder sb = new StringBuilder(80);
								sb.append(id).append(",").append(dateFormatter.format(timeStamp)).append(",").append(value).append("\n");
								byte[] b = sb.toString().getBytes("UTF-8")								
								cin.writeToCopy(b,0,b.length)																								
								dataFrames++;
								bc.lrts.put(id,timeStamp)
							}
							bc.lrt = timeStamp							
						}

						void onMessage(String origin, Date timeStamp, String message) {
							//log.debug("Insert message:${message}")
							endCopy()
							try {
								new Message(content:message,origin:origin,resultTime:timeStamp,type:"INFO").save()
								log.debug("Info message saved")
							} catch (SQLException e) {
								log.error(e)
							}
						}

						void onError(String origin, Date timeStamp, String message) {
							//log.debug("Insert error:${message}")
							endCopy()
							try {
								new Message(content:message,origin:origin,resultTime:timeStamp,type:"ERROR").save()
								log.debug("Error message saved")
							} catch (SQLException e) {
								log.error(e)
							}
						}
					}


			FrameParser p = new FrameParser(flatHandler)
			parseFrames(p,sender,frames)
			
			if (cin != null && cin.isActive()){
				long r = cin.endCopy()
			}			
			updateLrt(boardRecords)
			return true


		} catch (SQLException e){
			log.error(e.getMessage())
			return false
		} finally {			
			try {
				con.close()
			} catch (SQLException e){
				log.error(e.getMessage())
			}
		}
	}

}
