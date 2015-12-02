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
		Integer id
		Date lrt
		def channels = [:] 	// nr, id
		Integer lrssi
	}


	private Integer findOrSaveBoard(String origin){
		log.debug("findOrSaveBoard:${origin}")
		def db = new Sql(dataSource)
		try {
			def b = db.firstRow("select id from board where origin like ?;",[origin])
			if (b==null) {
				log.info("Creating new board:${origin}")
				def seq = db.firstRow("select nextval('board_id_seq') as id;")
				db.execute """ insert into board (id, origin) values (${seq.id},${origin});"""
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
			def c = db.firstRow("select id from channel c where board_id = ? and nr = ?;",[boardId, channelNr])
			if (c==null){				
				def b = db.firstRow("select deny_new_channels from board where id=?",[boardId])
				if (b.deny_new_channels == false){
					log.info("Creating new channel:${channelNr} for board:${boardId}")
					def seq = db.firstRow("select nextval('channel_id_seq') as id;")
					db.execute 	"""insert into channel (id, board_id, nr) values (${seq.id},${boardId},${channelNr});"""
					return seq.id					
				} else {
					log.info("Deny new channel:${channelNr} for board:${boardId}")
					return null
				}
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


 private void updateMetaInfo(BoardRecord boardRecord){
		def db = new Sql(dataSource)
		try {

			def channels = boardRecord.channels
			
			db.eachRow("""SELECT c.id as id, coalesce(c.sampling_interval, b.sampling_interval) as sampling_interval, 
												coalesce(check_delay,0) as check_delay,
												c.spline_id,
											    coalesce(c.critical_max, b.critical_max) as critical_max, 
												coalesce(c.critical_min, b.critical_min) as critical_min, 
												coalesce(c.warning_max, b.warning_max) as warning_max, 
												coalesce(c.warning_min, b.warning_min) as warning_min 												 											
										FROM channel c, board b WHERE b.id = c.board_id and b.id = ?""",[boardRecord.id]){ cha ->
																																
							if(channels.containsValue((int)cha.id)){
								log.debug("Update meta information for channel ${cha.id}")
								//	Get values 
								def obs = db.firstRow("""select real_value(coalesce(a.result_value,b.result_value),?) result_value, coalesce(a.result_time,b.result_time) result_time from
								(select * from observation where channel_id = ? order by result_time desc limit 1) a full outer join
								(select * from observation_exp where channel_id = ? order by result_time desc limit 1) b on (a.id = b.id);""",[cha.spline_id, cha.id, cha.id])
	
								def status_valid = null
								def status_valid_msg = null
	
								// Calc Validation 
								if (cha.critical_max !=null && ob.result_value > cha.critical_max){
									status_valid = 2; status_valid_msg = "Value ${obs.result_value} above ${cha.critical_max}."
								} else if (cha.warning_max != null && obs.result_value > cha.warning_max){
									status_valid = 1; status_valid_msg = "Value ${obs.result_value} above ${cha.warning_max}."
								} else if (cha.critical_min != null && obs.result_value < cha.critical_min){
									status_valid = 2; status_valid_msg = "Value ${obs.result_value} below ${cha.critical_min}."
								} else if (cha.warning_min != null && obs.result_value < cha.warning_min){
									status_valid = 1; status_valid_msg = "Value ${obs.result_value} below ${cha.warning_min}."
								} else {
									status_valid = 0; status_valid_msg = 'Value Ok';
								}
								
								// Calc counts 
								def lastCount = 0								
								if (cha.sampling_interval != null){									
									def s = db.firstRow("""select (a.n + b.n) as sum from 
												(select count(*) as n from observation where channel_id = :channel_id and result_time between
												(now() - ( (10*:sampling_interval+:check_delay) || ' seconds')::interval) and
												(now() - (:check_delay || ' seconds')::interval)) a, 								    
												(select count(*) as n from observation_exp where channel_id = :channel_id and result_time between
												(now() - ( (10*:sampling_interval+:check_delay) || ' seconds')::interval) and
												(now() - (:check_delay || ' seconds')::interval)) b""", ['channel_id':cha.id, 'sampling_interval':cha.sampling_interval,'check_delay':cha.check_delay])
									lastCount = s.sum		
									if (cha.check_delay == 0){
										lastCount = s.sum + 1
									} else {
										lastCount = s.sum
									}				
								}																							
								db.executeUpdate("update channel set last_result_value = ?, last_result_time = ?, status_valid = ?, status_valid_msg = ? , last_count = ? where id = ?",[obs.result_value, obs.result_time,status_valid,status_valid_msg, lastCount, cha.id])									
							}
							
							def b = db.firstRow("select max(last_result_time) lrt, max(status_valid) status from channel where board_id = ?", [boardRecord.id])						
							if (b.lrt == boardRecord.lrt){
								db.executeUpdate("update board set last_result_time = ?,status_valid = ?,last_rssi = ? where id = ?",[b.lrt,b.status,boardRecord.lrssi, boardRecord.id])
							} else {
								db.executeUpdate("update board set last_result_time = ?,status_valid = ? where id = ?",[b.lrt,b.status,boardRecord.id])
							}													
												
					}


		} catch (SQLException e){
			log.error(e.getMessage())
		} finally {
			db.close()
		}

	}


	def saveFrames(String sender, String[] frames) {
		if ((frames == null) || (sender == null)) return
			Connection con = null
		CopyIn cin = null
		def boardRecords = [:]
		def dataFrames = 0

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
								log.debug("endCopy")
								long r = cin.endCopy()
								cin = null
							}
						}


						void onNewOrigin(String origin) {
							log.debug("On new board:${origin}")
							endCopy()
							BoardRecord bc = boardRecords[origin]
							if (bc == null){
								boardRecords[origin] = new BoardRecord(id:FrameService.this.findOrSaveBoard(origin))
							}
						}

						void onNewChannels(String origin, SortedSet<Integer> channels) {
							log.debug("On new channels:${channels} for board:${origin}")
							endCopy()
							BoardRecord bc = boardRecords[origin]
							assert(bc!=null)
							channels.removeAll(bc.channels.keySet())
							for (Integer nr:channels){
								bc.channels[nr] = FrameService.this.findOrSaveChannel(bc.id,nr)
							}
						}

						void onDataFrame(String origin, Date timeStamp, Hashtable<Integer,Float> values, Integer rssi) {
							log.debug("On dataFrame:${values} for board:${origin}")
							startCopy()
							BoardRecord bc = boardRecords[origin]
							assert(bc!=null)

							for (Map.Entry<Integer, Float> item : values.entrySet()) {
								Integer nr  = item.getKey()
								Float value  = item.getValue()
								Integer id = bc.channels[nr]
								if (id != null){
									StringBuilder sb = new StringBuilder(80);
									sb.append(id).append(",").append(dateFormatter.format(timeStamp)).append(",").append(value).append("\n");
									byte[] b = sb.toString().getBytes("UTF-8")
									cin.writeToCopy(b,0,b.length)
								}
								dataFrames++;
							}
							bc.lrt = timeStamp
							bc.lrssi = rssi
						}

						void onMessage(String origin, Date timeStamp, String message) {
							log.debug("Insert message:${message}")
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

			// Update channel and board meta data
			boardRecords.each{ bc ->
				updateMetaInfo(bc.value)
			}

			log.info("${dataFrames} observations imported")
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
