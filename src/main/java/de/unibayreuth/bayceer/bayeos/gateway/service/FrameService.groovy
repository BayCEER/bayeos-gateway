package de.unibayreuth.bayceer.bayeos.gateway.service

import java.sql.Connection
import java.sql.SQLException
import java.text.SimpleDateFormat
import javax.script.ScriptEngine
import javax.sql.DataSource

import org.apache.log4j.Logger
import org.postgresql.PGConnection
import org.postgresql.copy.CopyIn
import org.postgresql.copy.CopyManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import bayeos.frame.FrameParserException
import bayeos.frame.Parser
import bayeos.frame.types.ByteFrame
import bayeos.frame.types.MapUtils
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEvent
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEventProducer
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEventType
import de.unibayreuth.bayceer.bayeos.gateway.event.NewBoardEvent
import de.unibayreuth.bayceer.bayeos.gateway.event.NewChannelEvent
import de.unibayreuth.bayceer.bayeos.gateway.event.NewMessageEvent
import de.unibayreuth.bayceer.bayeos.gateway.event.NewObservationEvent
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository
import de.unibayreuth.bayceer.bayeos.gateway.repo.VirtualChannelRepository
import groovy.sql.Sql
import org.apache.commons.codec.binary.Base64


@Service
class FrameService {

	@Autowired
	DataSource dataSource

	@Autowired
	ScriptEngine scriptEngine

	@Autowired
	VirtualChannelRepository vcRepo

	@Autowired
	FrameEventProducer eventProducer

	@Autowired
	BoardRepository boardRepo

	private Logger log = Logger.getLogger(FrameService.class)

	class Board {
		Integer domainId
		String origin
		Integer id
		Date lrt
		def channels = [:] 	// nr, id
		def vchannels = [:] // channel_id, vc
		Integer lrssi
		Long records = 0
	}
	
	def boolean saveFrame(String sender, ByteFrame frame) {
		return saveFrames(null,sender,[Base64.encodeBase64String(frame.getBytes())] as List<String>)
	}

	def boolean saveFrames(Long domainId, String sender, List<String> frames) {
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
			
			for(String f:frames) {
				try {
					if (log.debugEnabled){
						log.debug("Frame:" + f);
					}
					def res = Parser.parseBase64(f,new Date(),sender,null)
					
					if (log.debugEnabled){
						log.debug("Parsed:" + MapUtils.toString(res));
					}

					// Parser sends ts as nano secs
					Date ts = new Date((long)(res['ts']/(1000*1000)))

					// Check if board exists
					Board b = boards[res['origin']]
					if (b == null) {																								
						Long id = findOrSaveBoard(db,domainId,res['origin'])
						b = new Board(id:id, domainId: domainId, origin:res['origin'], lrt:ts,lrssi:res['rssi'], channels:[:],
							 vchannels: vcRepo.findByBoardId(id))
						boards[res['origin']] = b
					} else {
						b.lrt = ts
						b.lrssi = res['rssi']
					}

					switch (res['type']) {
						case "DataFrame":
							res['value'].each { key, value ->
								if (!b.channels.containsKey(key)){
									// log.info("Board:" + b.channels.toMapString())
									b.channels[key] = findOrSaveChannel(db,b,key)
								}
							}

							// Create virtual channels
							for (VirtualChannel vc: b.vchannels){
								b.channels[vc.getNr()] = findOrSaveChannel(db,b,vc.getNr())
							}

							// Write original values out using copy
							CopyManager cm = ((PGConnection)con.unwrap(PGConnection.class)).getCopyAPI()
							cin = cm.copyIn("COPY observation (channel_id,result_time,result_value) FROM STDIN WITH CSV")
							res['value'].each { key, value ->								
								def chaId = b.channels[key]
								if (chaId != null && value != null) {									
									Float fvalue = (Float)value	
									if (!fvalue.isNaN()){
										StringBuffer sb = new StringBuffer(200)
										sb.append(chaId).append(",").append(dateFormatter.format(ts)).append(",").append(fvalue).append("\n")
										byte[] pl = sb.toString().getBytes("UTF-8")
										cin.writeToCopy(pl,0,pl.length)
										sb = null
										b.records++
									}										
								}
																															
							}


							// Write calculated values out
							for (VirtualChannel vc: b.vchannels){
								try {									
									def chaId = b.channels[vc.getNr()]
									def vcValue = vc.eval(scriptEngine, res['value'])									
									if (chaId != null && vcValue != null){																														
										Float value = (Float)vcValue;
											if (!value.isNaN()) {
												StringBuffer sb = new StringBuffer(200)
												sb.append(chaId).append(",").append(dateFormatter.format(ts)).append(",").append(value).append("\n")
												byte[] pl = sb.toString().getBytes("UTF-8")
												cin.writeToCopy(pl,0,pl.length)
												sb = null
												b.records++
											}
									}
									
								} catch (Exception e){
									log.warn("Failed to calculate virtual channel value:${vc.nr}")
									log.error(e.getMessage())
								}
							}

							long r = cin.endCopy()
							break
						case "Message":
							db.executeInsert("insert into message (board_id, content, result_time, type) values (?,?,?,?);",[b.id, res['value'], ts.toTimestamp(), "INFO"])
							eventProducer.addFrameEvent(new NewMessageEvent(b.id))
							log.debug("Message saved")
							break
						case "ErrorMessage":
							db.executeInsert("insert into message (board_id, content, result_time, type) values (?,?,?,?);",[b.id, res['value'], ts.toTimestamp(), "ERROR"])							
							eventProducer.addFrameEvent(new NewMessageEvent(b.id))
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
			boards.each{ id, board ->
				updateMetaInfo(db, board)
				log.info("${board.records} observations for board ${board.origin} imported")				
				eventProducer.addFrameEvent(new NewObservationEvent(board.id,board.origin,board.records))								
			}			
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




	private Long findOrSaveBoard(Sql db, Long domainId, String origin) throws SQLException {
		def b;	
		if (domainId == null) {
			b = db.firstRow("select id from board where origin like ? and (domain_id is null or domain_id_created is null);",[origin])
		} else {
			b = db.firstRow("select id from board where origin like ? and domain_id = ?;",[origin, domainId])			
		}
					
		if (b==null) {
			log.info("Creating new board:${origin}")
			def seq = db.firstRow("select nextval('board_id_seq') as id;")
			db.execute """insert into board (id,domain_id,domain_id_created, origin) values (${seq.id},${domainId},${domainId},${origin});"""
			eventProducer.addFrameEvent(new NewBoardEvent(seq.id))
			return seq.id
		} else {
			return b.id
		}
	}

	private Long findOrSaveChannel(Sql db, Board board, String channelNr) throws SQLException {		
		def c = db.firstRow("select id from channel c where board_id = ? and nr = ?;",[board.id, channelNr])
		if (c==null){
			def b = db.firstRow("select deny_new_channels from board where id=?",[board.id])
			if (b.deny_new_channels == false){
				log.info("Creating new channel:${channelNr} for board:${board.id}")
				def seq = db.firstRow("select nextval('channel_id_seq') as id;")
				db.execute 	"""insert into channel (id, board_id, nr) values (${seq.id},${board.id},${channelNr});"""
				eventProducer.addFrameEvent(new NewChannelEvent(board.id))
				return seq.id
			} else {
				log.info("Deny new channel:${channelNr} for board:${board.id}")
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
											    c.critical_max, 
												c.critical_min, 
												c.warning_max, 
												c.warning_min									 											
										FROM channel c, board b WHERE b.id = c.board_id and b.id = ?""",[board.id]){ cha ->

					if(channels.containsValue(cha.id)){
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
							db.executeUpdate("update channel set last_result_value = ?, last_result_time = ?, status_valid = ?, status_valid_msg = ? , last_count = ? where id = ? and (last_result_time is null or last_result_time < ?)",
								[obs.result_value, obs.result_time, status_valid, status_valid_msg.toString(), lastCount, cha.id, obs.result_time])
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
