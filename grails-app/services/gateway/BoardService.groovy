package gateway

import frame.parser.FrameHandler;
import frame.parser.FrameParser
import frame.parser.ParserException;
import groovy.sql.Sql

import java.awt.GraphicsConfiguration.DefaultBufferCapabilities;
import java.awt.image.RescaleOp;
import java.beans.Transient;
import java.sql.Array
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.util.Date
import java.util.Hashtable;
import java.util.Map
import java.util.zip.ZipEntry
import java.util.zip.ZipFile;

import javax.xml.bind.JAXB

import org.postgresql.PGConnection
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection

import de.unibayreuth.bayceer.parser.simpleformat.CSVParser
import de.unibayreuth.bayceer.parser.simpleformat.Column
import de.unibayreuth.bayceer.parser.simpleformat.SimpleFormat
import org.springframework.transaction.annotation.Transactional


class BoardService  {
	
	
	def dataSource 	
	def chkProps = ['samplingInterval','criticalMax','criticalMin','warningMin','warningMax']	
	def chaProps = chkProps + ['nr','label','phenomena','unit','spline','aggrInterval','aggrFunction']
		
		
	
	BoardTemplate saveAsTemplate(Board board){		
		def template = new BoardTemplate(name:"New Template", revision:"1.0")		
		chkProps.each{ p ->
			template.setProperty(p, board.getProperty(p));
		}
		template.save();
				
		def channels = Channel.findAllByBoard(board)
		channels.each{ cha ->
			ChannelTemplate cht = new ChannelTemplate()			
			chaProps.each{ p ->
				cht.setProperty(p, cha.getProperty(p))
			}
			cht.setBoardTemplate(template);
			cht.save();			
		}										
		return template
	}
	
	
	def applyTemplate(Board board, BoardTemplate template){
		chkProps.each{ p ->
			board.setProperty(p, template.getProperty(p))
		}
		template.channelTemplates.each{ cht ->
			Channel ch = Channel.findOrSaveWhere(board:board,nr:cht.nr)
			chaProps.each{ p ->				
				ch.setProperty(p, cht.getProperty(p))				
			}
		    ch.save(flush:true)
		}	
		board.save(flush:true)
	}	
	
	
	def getChannelStati(Board board) {
		def db = new Sql(dataSource)
		def channelStati = db.rows("""SELECT c.id, c.nr, c.label, unit.abbrevation as unit, c.status_valid, c.status_valid_msg, 
				s.status_complete, s.status_complete_msg, c.last_result_time, c.last_result_value, not c.exclude_from_nagios as nagiosOn  
				FROM channel_status s, channel c LEFT JOIN unit on unit.id = c.unit_id where c.board_id = ? and s.id = c.id  order by c.nr""", board.id)
		db.close()
		return channelStati
	}
	
	def getBoardStatus(Board board) {
		 def db = new Sql(dataSource)
		 def boardStatus = db.firstRow("SELECT status_valid, status_complete, last_result_time FROM board_status where id = ?", board.id)
		 db.close()
		 return boardStatus
	}
	
	
	def findBoardsByGroup(Long groupId, Integer max, Integer offset) {
		def db = new Sql(dataSource)
		def result = db.rows("""SELECT bs.group_id, bs.group_name, bs.id, bs.origin, bs.name, bs.last_rssi, bs.last_result_time,
				greatest(bs.status_valid,bs.status_complete) as nagiosStatus, ns.exc_cha as nagiosChannelOff, not b.exclude_from_nagios as nagiosOn
				FROM board b, board_status bs, (SELECT board_id, count(*) as total, sum(exclude_from_nagios::int)::int as exc_cha FROM channel GROUP BY board_id) as ns
				where b.id = bs.id and bs.id = ns.board_id and group_id = ?  order by bs.group_name, bs.origin LIMIT ? OFFSET ?""",[groupId, max, offset])
		db.close()
		return result			   
	}
	
	def findAllBoards(Integer max, Integer offset) {
		def db = new Sql(dataSource)
		def result = db.rows("""SELECT bs.group_id, bs.group_name, bs.id, bs.origin, bs.name, bs.last_rssi, bs.last_result_time,
                greatest(bs.status_valid, bs.status_complete) as nagiosStatus, ns.exc_cha as nagiosChannelOff, not b.exclude_from_nagios as nagiosOn
				FROM board b, board_status bs, (SELECT board_id, count(*) as total, sum(exclude_from_nagios::int)::int as exc_cha FROM channel GROUP BY board_id) as ns
				where b.id = bs.id and bs.id = ns.board_id order by bs.group_name, bs.origin LIMIT ? OFFSET ?""",[max, offset])
		db.close()
		return result
	}
	
	
	
	
	def int createBoardByFileFormat(String origin, FileFormat format){
		SimpleFormat sf = JAXB.unmarshal(new StringReader(format.getXml()), SimpleFormat.class)		
		def db = new Sql(dataSource)		
		def seq = db.firstRow("select nextval('board_id_seq') as id;")		 			
		db.execute """
			insert into board (id, origin, frame_storage) 
			values (${seq.id},${origin},true); 
		"""				
		for(int i=1;i<sf.getColumns().size();i++){
			Column col = sf.getColumns().get(i)
			db.execute "insert into channel (board_id,nr,label) values (${seq.id},${col.num},${col.name}); "			
		}																																												
		db.execute 'CREATE TABLE _board_data_' + seq.id + ' (id bigserial, result_time timestamp with time zone);'
		db.execute 'CREATE INDEX  idx_board_data_' + seq.id + ' ON _board_data_' + seq.id + ' (result_time);'
		for(int i=1;i<sf.getColumns().size();i++){
			db.execute 'ALTER TABLE _board_data_' + seq.id + ' add column c' + i + ' real;'				
		}			
		db.close()			
		return seq.id					
	}
	
	def int copyZippedData(Integer id, CSVParser parser, File file){		
		int ret = 0								
		ZipFile zf = new ZipFile(file)		
		for(ZipEntry entry : Collections.list( zf.entries() ) ){			
			InputStream inStream = zf.getInputStream( entry )
			ret = ret + copyData(id, parser, inStream);							
		}		
		return ret
	}
	
	def int copyData(Integer id, CSVParser parser, InputStream source){
					
		def db = new Sql(dataSource)
		List cha = db.rows('select nr from channel where board_id = :id order by nr',[id:id])				 
				
		def copy = "COPY _board_data_${id} (result_time," + cha.collect{"c${it[0]}"}.join(",") +  ") FROM STDIN WITH CSV"
		int rows = 0		
		int batchSize = 1000;						
		try {									
			Connection con = dataSource.getConnection()
			CopyManager copyManager = ((PGConnection)con.unwrap(PGConnection.class)).getCopyAPI()			
			StringBuffer b = new StringBuffer(10000)						
			Scanner scanner = new Scanner(source)						
			while(scanner.hasNext()){													
				b.append(parser.parse(scanner.nextLine())).append("\n")				
				if (rows%batchSize==0){
					copyManager.copyIn(copy,new StringReader(b.toString()));
					b.setLength(0)					
				}
				rows++;
			}
			
			
			if (b.size()>0)	copyManager.copyIn(copy,new StringReader(b.toString()))			
			db.eachRow('select id, nr from channel where board_id = ? order by nr',[id]){ row ->				
				Timestamp ts = new Timestamp(parser.getColumnLastDate(row.nr).getTime())				
				db.executeUpdate("update channel set last_result_time = ? where id = ? and (last_result_time < ? or last_result_time is null)",[ts,row.id,ts])
			}
			Timestamp bm = new Timestamp(parser.getLastDate().getTime())
			db.executeUpdate("update board set last_result_time = ? where id = ? and (last_result_time < ? or last_result_time is null)",[bm,id,bm])			
			db.close()
			return rows;
			
		} catch (Exception e){
			log.error(e.getMessage())
			rows = 0						
		}	
		
		return rows
	}


	
}
