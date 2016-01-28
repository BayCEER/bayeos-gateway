package gateway

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
		def channelStati = db.rows("""SELECT c.id, c.nr, c.label, unit.abbrevation as unit, chk.status_valid, chk.status_valid_msg, 
				chk.status_complete, chk.status_complete_msg, c.last_result_time, c.last_result_value, not c.exclude_from_nagios as nagiosOn  
				FROM channel_check chk, channel c LEFT JOIN unit on unit.id = c.unit_id where c.board_id = ? and chk.id = c.id  order by c.nr""", board.id)
		db.close()
		return channelStati
	}
	
	def getBoardStatus(Board board) {
		 def db = new Sql(dataSource)
		 def boardStatus = db.firstRow("SELECT chk.status_valid, chk.status_complete, b.last_result_time FROM board b, board_check chk where b.id = chk.id and b.id = ?", board.id)
		 db.close()
		 return boardStatus
	}
	
	
	def findBoardsByGroup(Long groupId, Integer max, Integer offset) {
		def db = new Sql(dataSource)						
		def result = db.rows("""SELECT bg.id as group_id, bg.name as group_name, b.id, b.origin, b.name, b.last_rssi, b.last_result_time,
				chk.status as nagiosStatus, ns.exc_cha as nagiosChannelOff, not b.exclude_from_nagios as nagiosOn
				FROM board b left join board_group bg on bg.id = b.board_group_id, board_check chk, (SELECT board_id, count(*) as total, sum(exclude_from_nagios::int)::int as exc_cha FROM channel GROUP BY board_id) as ns
				where b.id = chk.id and b.id = ns.board_id and bg.id = ? order by bg.name, b.origin LIMIT ? OFFSET ?""",[groupId, max, offset])
		db.close()
		return result			   
	}
	
	def findAllBoards(Integer max, Integer offset) {
		def db = new Sql(dataSource)
		def result = db.rows("""SELECT bg.id as group_id, bg.name as group_name, b.id, b.origin, b.name, b.last_rssi, b.last_result_time,
                chk.status as nagiosStatus, ns.exc_cha as nagiosChannelOff, not b.exclude_from_nagios as nagiosOn
				FROM board b left join board_group bg on bg.id = b.board_group_id, board_check chk, (SELECT board_id, count(*) as total, sum(exclude_from_nagios::int)::int as exc_cha FROM channel GROUP BY board_id) as ns
				where b.id = chk.id and b.id = ns.board_id order by bg.name, b.origin LIMIT ? OFFSET ?""",[max, offset])
		db.close()
		return result
	}

	
}
