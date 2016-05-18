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
	def chkProps = ['samplingInterval','criticalMax','criticalMin','warningMin','warningMax','checkDelay']	
	def chaProps = chkProps + ['nr','label','phenomena','unit','spline','aggrInterval','aggrFunction']
		
		
	
	BoardTemplate saveAsTemplate(Board board){		
		def template = new BoardTemplate(name:"New Template", revision:"1.0")		
		chkProps.each{ p ->
			template.setProperty(p, board.getProperty(p));
		}
		template.save(flush:true);
				
		def channels = Channel.findAllByBoard(board)
		channels.each{ cha ->
			ChannelTemplate cht = new ChannelTemplate()	
					
			chaProps.each{ p ->
				cht.setProperty(p, cha.getProperty(p))
			}
			cht.setBoardTemplate(template);
			cht.save(flush:true);			
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
				FROM channel_check chk, channel c LEFT JOIN unit on unit.id = c.unit_id where c.board_id = ? and chk.id = c.id  order by get_order_number(c.nr)""", board.id)
		db.close()
		return channelStati
	}
	
	def findBoards(String search, def sort) {
		def db = new Sql(dataSource)
		def sql = """SELECT bg.id as group_id, bg.name as group_name, b.id, b.origin, b.name, b.last_rssi,
						extract(epoch from date_trunc('milliseconds',b.last_result_time)) * 1000 as last_result_time, board_check.status
						FROM board b left outer join board_group bg on (b.board_group_id = bg.id) join board_check on (b.id = board_check.id)"""
		if (search){
			sql = sql + " where bg.name ilike :s or b.origin ilike :s or b.name ilike :s"
		} 										 
		sql = sql + " order by " + sort.join(",")
		def rows	
		if (search) {
			rows = db.rows(sql,s:search)
		} else {
			rows = db.rows(sql)
		}
		
		db.close()
		return rows
	}
	

	
}
