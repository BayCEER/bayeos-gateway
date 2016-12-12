package de.unibayreuth.bayceer.bayeos.gateway.service

import java.sql.Array
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Hashtable
import java.util.List
import java.util.Map
import javax.sql.DataSource
import groovy.sql.Sql
import org.apache.log4j.Logger
import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.postgresql.copy.CopyIn
import bayeos.frame.FrameParserException
import bayeos.frame.Parser

@Service
class FrameService {

	@Autowired
	DataSource dataSource
	
	private Logger log = Logger.getLogger(FrameService.class)
	
	class Board {
		Integer id
		Date lrt
		def channels = [:] 	// nr, id
		Integer lrssi
	}
 
	def saveFrames(String sender, List<String> frames) {
		if ((frames == null) || (sender == null)) return
		
		Connection con = null
		CopyIn cin = null
		
		def boards = [:]
						
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS+00")
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				
		try {
			con = dataSource.getConnection()
			Sql db = new Sql(con)
			log.info("Parsing ${frames.size()} frames from: ${sender}")
			long obsCount = 0
			for(String f:frames) {
				try {
					def res = Parser.parseBase64(f,new Date(),sender,null)
					
					// Parser sends ts as nano secs
					Date ts = new Date((long)(res['ts']/(1000*1000)))					

					// Check if board exists
					Board b = boards[res['origin']]					 				
					if (b == null) {							
						b = new Board(id:findOrSaveBoard(db,res['origin']), lrt:ts,lrssi:res['rssi'], channels:[:])
						boards[res['origin']] = b			
					} else {						
						b.lrt = ts
						b.lrssi = res['rssi']
					}
					
					switch (res['type']) {						
						case "DataFrame":
							res['data'].each { key, value ->								
								if (!b.channels.containsKey(key)){									
									// log.info("Board:" + b.channels.toMapString())																		
									b.channels[key] = findOrSaveChannel(db,b.id,key)
								}
							}							
							// Insert channel values from data using copy 
							CopyManager cm = ((PGConnection)con.unwrap(PGConnection.class)).getCopyAPI()
							cin = cm.copyIn("COPY observation (channel_id,result_time,result_value) FROM STDIN WITH CSV")							
							res['data'].each { key, value ->	
								Float fvalue = (Float)value
								if (!fvalue.isNaN()){
									StringBuffer sb = new StringBuffer(200)
									sb.append(b.channels[key])
									sb.append(",").append(dateFormatter.format(ts))
									sb.append(",").append(fvalue).append("\n")
									byte[] pl = sb.toString().getBytes("UTF-8")
									cin.writeToCopy(pl,0,pl.length)
									sb = null
									obsCount++
								}										
							}
							long r = cin.endCopy()							
							break
						case "Message":							
							db.executeInsert("insert into message (content, origin, result_time, type) values (?,?,?,?);",[res['value'], res['origin'], ts.toTimestamp(),"INFO"])
							log.debug("Message saved")
							break
						case "ErrorMessage":
							db.executeInsert("insert into message (content, origin, result_time, type) values (?,?,?,?);",[res['value'], res['origin'], ts.toTimestamp(),"ERROR"])
							log.debug("ErrorMessage saved")
							break
						default:
							break
					}														
				} catch (FrameParserException e){
					log.warn("Failed to parse frame:${f} Error:${e.getMessage()}")
				}
			}
					
			// Update channel and board meta data
			boards.each{ board ->
				updateMetaInfo(db, board.value)
			}
				
			log.info("${obsCount} observations imported")
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



	private Integer findOrSaveBoard(Sql db, String origin) throws SQLException {
		log.debug("findOrSaveBoard:${origin}")
		def b = db.firstRow("select id from board where origin like ?;",[origin])
		if (b==null) {
			log.info("Creating new board:${origin}")
			def seq = db.firstRow("select nextval('board_id_seq') as id;")
			db.execute """ insert into board (id, origin) values (${seq.id},${origin});"""
			return seq.id
		} else {
			return b.id
		}
	}

	private Integer findOrSaveChannel(Sql db, Integer boardId, String channelNr) throws SQLException {
		log.debug("findOrSaveChannel: Board id:${boardId},Channel nr:${channelNr}")
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
	}

	
	private void updateMetaInfo(Sql db, Board board) throws SQLException {

		def channels = board.channels
		db.eachRow("""SELECT c.id as id, coalesce(c.sampling_interval, b.sampling_interval) as sampling_interval, 
												coalesce(c.check_delay,b.check_delay,0) as check_delay,
												c.spline_id,
											    coalesce(c.critical_max, b.critical_max) as critical_max, 
												coalesce(c.critical_min, b.critical_min) as critical_min, 
												coalesce(c.warning_max, b.warning_max) as warning_max, 
												coalesce(c.warning_min, b.warning_min) as warning_min 												 											
										FROM channel c, board b WHERE b.id = c.board_id and b.id = ?""",[board.id]){ cha ->

					if(channels.containsValue((int)cha.id)){
						log.debug("Update meta information for channel ${cha.id}")
						//	Get values
						def obs = db.firstRow("""select real_value(result_value,?) result_value, result_time from
								(select * from (select * from observation  where channel_id = ? order by result_time desc limit 1) a
								union select * from (select * from observation_exp where channel_id = ? order by result_time desc limit 1) b)
								c order by result_time desc limit 1;""",[cha.spline_id, cha.id, cha.id])

						if (obs!=null){

							def status_valid = null
							def status_valid_msg = null

							// Calc Validation
							if (cha.critical_max !=null && obs.result_value > cha.critical_max){
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
							db.executeUpdate("update channel set last_result_value = ?, last_result_time = ?, status_valid = ?, status_valid_msg = ? , last_count = ? where id = ?",[obs.result_value, obs.result_time, status_valid, status_valid_msg.toString(), lastCount, cha.id])
						}

						def b = db.firstRow("select max(last_result_time) lrt, max(status_valid) status from channel where board_id = ?", [board.id])
						if (b.lrt == board.lrt){
							db.executeUpdate("update board set last_result_time = ?,status_valid = ?,last_rssi = ? where id = ?",[b.lrt, b.status, board.lrssi, board.id])
						} else {
							db.executeUpdate("update board set last_result_time = ?,status_valid = ? where id = ?",[b.lrt, b.status, board.id])
						}

					}
				}
	}
	


	
}
