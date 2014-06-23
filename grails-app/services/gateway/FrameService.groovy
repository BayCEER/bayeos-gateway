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
import frame.service.Frame
import frame.parser.AbstractFrameHandler
import frame.parser.FrameHandler
import frame.parser.FrameParser
import frame.parser.FramePrintHandler
import frame.parser.ParserException
import gateway.Message
import groovy.sql.Sql
import org.apache.commons.codec.binary.Base64
import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager


class FrameService {

	def dataSource
	def boardService
	
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS+01")
	
			
	private void parseFrames(FrameParser parser, String sender, Date from, String[] frames) {
		if (frames == null)	return
		log.info("Received ${frames.length} frames from: ${sender}")
		long errorFrameCount = 0
		
		for(String f:frames) {
			try {				
				if (!Base64.isBase64(f)) {
					log.warn("Encoded frame:" + f + " contains invalid Base64 characters.")
					errorFrameCount++
				} else {
					parser.parse(Base64.decodeBase64(f),sender,from,null)
				}								
			} catch (ParserException e){
				log.error("FrameParserError: Sender:${sender} Date:${from} Frame:${f}")
				errorFrameCount++
			}
		}		
		log.info("${frames.length - errorFrameCount}/${frames.length} frames saved")
		
	}
	
	def saveMatrixFrames(String sender, Date from, String[] frames) {		
		MatrixHandler h = new MatrixHandler()
		FrameParser parser = new FrameParser()
		parser.setFrameHandler(h)
		parseFrames(parser,sender,from,frames)			
		def db = new Sql(dataSource)		
		h.boardRecords.each{ origin, br ->			
			br.lrtChas.each { nr, date ->			
				db.executeUpdate("update channel set last_result_time = ? where board_id = ? and nr = ?",[new Timestamp(date.getTime()),br.id,nr])				
			}			 					
			db.executeUpdate("update board set last_result_time = ? where id = ?",[new Timestamp(br.lrt.getTime()),br.id])
		}
		db.close()
		parser = null			
		return true
	}
	
	def saveFlatFrames(String sender, Date from, String[] frames) {
		FrameParser parser = new FrameParser()
		parser.setFrameHandler(new FlatHandler())
		parseFrames(parser, sender, from, frames)
		parser = null		
		return true
	}

	
	
	
	class FlatHandler extends AbstractFrameHandler {
		@Override 
		public void data(String origin, Date result_time, Integer rssi,	Hashtable<Integer, Float> data)  {			
			if (data == null || data.size() == 0){
				return
			}
			def db = new Sql(dataSource)
			try {
				def ts = new Timestamp(result_time.getTime())
				def cha = dataSource.getConnection().createArrayOf("int", data.keySet().toArray())
				def val = dataSource.getConnection().createArrayOf("float",data.values().toArray())
				def r = db.call("{call insert_observation($origin,$ts,$rssi,$cha,$val)}")
				log.debug("Frame data saved")
	
			} catch (SQLException e) {
				log.error(e)
				throw new ParserException(e.getMessage())
			} finally {
				db.close()
			}
		}
	
		@Override
		public void message(String o, Date timeStamp, String message)	 {
			try {				
				new Message(content:message,origin:o,result_time:timeStamp, type:"INFO").save()
				log.debug("Info message saved")
			} catch (SQLException e) {
				log.error(e)				
			}
	
		}
	
		@Override
		public void error(String o, Date timeStamp, String message)  {
			try {				
				new Message(content:message,origin:o,result_time:timeStamp, type:"ERROR").save()				
				log.debug("Error message saved")
			} catch (SQLException e) {
				log.error(e)
				
			}
	
		}
				
	
		
	}
	
	class BoardRecord {		
		def id
	    def chas = []
		def lrt
		def lrtChas = [:]		
	}	
	
	class MatrixHandler extends AbstractFrameHandler {				
		
		// A temporary cash to omit multiple checks on board and channel definition 
		def boardRecords = [:]
					
		@Override
		public void data(String origin, Date result_time, Integer rssi,	Hashtable<Integer, Float> data) {
			def db = new Sql(dataSource)						
			if (data == null || data.size() == 0 || origin == null || result_time == null){
				return
			}										
			BoardRecord bc = boardRecords.get(origin);											
			if (bc==null) {				
				def s = db.firstRow("select id from board where origin like ?;",[origin])							
				if (s==null) {
					log.info("Create board:${origin}")
					def seq = db.firstRow("select nextval('board_id_seq') as id;")
					db.execute """
							insert into board (id, origin, frame_storage) 
							values (${seq.id},${origin},true); 
					"""												
					db.execute 'CREATE TABLE _board_data_' + seq.id + ' (id bigserial, result_time timestamp with time zone);'
					db.execute 'CREATE INDEX idx_board_data_' + seq.id + ' ON _board_data_' + seq.id + ' (result_time);'
					bc = new BoardRecord(id:seq.id,lrt:result_time)					
				} else {				
					bc = new BoardRecord(id:s.id,lrt:result_time)
				}				
				boardRecords.put(origin,bc)
			} else {
				bc.lrt = result_time
			}
													   
			data.each { key, value ->			
				bc.lrtChas.put(key,result_time)				
				if (!bc.chas.contains(key)) {
					log.debug("Check column:${key}")
					def r = db.firstRow('select nr from channel where board_id = ? and nr = ?',[bc.id,key])
					if (r == null) {
						db.execute "insert into channel (board_id,nr) values (?,?)",[bc.id,key]
						db.execute 'ALTER TABLE _board_data_' + bc.id + ' add column c' + key + ' real;'
					} 					
					bc.chas.add(key)					
				}				
			}
			Connection con = dataSource.getConnection()
			CopyManager cm  = ((PGConnection)con.unwrap(PGConnection.class)).getCopyAPI()			
			StringBuffer b = new StringBuffer(200);
			b.append(dateFormatter.format(result_time)).append(",")
			b.append(data.values().join(",")).append("\n")			
			cm.copyIn("COPY _board_data_${bc.id} (result_time," + data.keySet().collect{"c${it}"}.join(",") +  ") FROM STDIN WITH CSV",
				new StringReader(b.toString()))						 									
			db.close()
		}
		
	}
	


}
