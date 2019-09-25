package de.unibayreuth.bayceer.bayeos.gateway.service

import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

import de.unibayreuth.bayceer.bayeos.gateway.model.NagiosMessage
import groovy.sql.Sql
import java.sql.SQLException

@Service
class NagiosService {

	@Autowired
	DataSource dataSource
	Logger log = Logger.getLogger(NagiosService.class)
	
	NagiosMessage msgGateway() {
		log.info("Query status of gateway")
		def db = new Sql(dataSource)
		NagiosMessage m = getNagiosMsg(db.rows("select * from nagios_status"))
		db.close()
		return m;
	}
		
	NagiosMessage msgDomain(Integer id) {
		log.info("Query status of domain: ${id}")
		def db = new Sql(dataSource)
		if (db.rows("select true from domain where id = ?",[id]).empty ){
			db.close();
			return new NagiosMessage(status:3,text:"Domain ${id} not found");
		} else {
			List channels = db.rows("select * from nagios_status where domain_id  =  ?",[id])
			db.close()
			NagiosMessage m = getNagiosMsg(channels)
			return m;
		}		
	}

	NagiosMessage msgBoardGroup(Integer id) {
		log.info("Query status of board group: ${id}")
		def db = new Sql(dataSource)
		if (db.rows("select true from board_group where id = ?",[id]).empty ){
			db.close();
			return new NagiosMessage(status:3,text:"Group ${id} not found");
		} else {
			List channels = db.rows("select * from nagios_status where board_group_id = ?",[id])
			db.close()
			NagiosMessage m = getNagiosMsg(channels)
			return m;
		}
	}

	NagiosMessage msgBoard(Integer id) {
		log.info("Query status of board: $id")
		def db = new Sql(dataSource)
		if (db.rows("select true from board where id=?",[id]).empty){
			db.close();
			return new NagiosMessage(status:3,text:"Board with id:${id} not found");
		} else {
			List channels = db.rows("select * from nagios_status where board_id=?",[id]);
			db.close()
			NagiosMessage m = getNagiosMsg(channels)
			return m;
		}
	}


	NagiosMessage msgChannel(Integer id) {
		log.info("Query status of channel: ${id}")
		def db = new Sql(dataSource)
		if (db.rows("select true from channel where id=?",[id]).empty){
			db.close();
			return new NagiosMessage(status:3,text:"Channel with id:${id} not found");
		} else {
			List channels = db.rows('select * from nagios_status where channel_id=?',[id])
			db.close()
			NagiosMessage m = getNagiosMsg(channels)
			return m;
		}
	}

		
	
	NagiosMessage msgExporter() {
		log.info("Query status of export job")
		def db = new Sql(dataSource)				
		try {							
			def r = db.firstRow("""
						select result_value from board join channel on (board.id = channel.board_id) 
						join all_observation on all_observation.channel_id = channel.id 
				 		where board.origin like '\$SYS/ExporterJob' and channel.nr like 'exit' order by result_time desc limit 1;""")						 
			if (r != null){
					if (r.result_value > 0){
						return new NagiosMessage(text:"Job finished succesfully.",status:0)
					} else {
						// Critical job
						return new NagiosMessage(text:"Job failed with error.",status:2)
					}
			} else {
					// 	Unknown
					return new NagiosMessage(text:"No job status found.", status:3)
			}
												
		} catch (SQLException e){
			log.error(e.getMessage())
		} finally {
			db.close()
		}

	}

	private NagiosMessage getNagiosMsg(List channels){
		def out = new StringBuffer(200)
		def maxStat = 0
		def boardGroupName
		def domainName
		def boardOrigin
		channels.each{
			
			// Domain
			if (it.domain_name != domainName){
				out.append("Domain[${it.domain_name}]\n")
				domainName = it.domain_name
			}
			
			// Group
			if (it.board_group_name != boardGroupName){
				out.append("Board Group[${it.board_group_name}]\n")
				boardGroupName = it.board_group_name
			}
			// Board
			if (it.board_origin != boardOrigin){
				out.append(" Board[${it.board_origin}]\n")
				boardOrigin = it.board_origin
			}
			// Channel
			out.append("  Ch[${it.channel_nr}]:")
			if (it.status_complete>0){
				out.append("C:=${it.status_complete_msg}")
			}
			if (it.status_valid>0){
				out.append("V:=${it.status_valid_msg}")
			}
			// Status
			if (it.status_complete>maxStat){
				maxStat = it.status_complete
			}
			if (it.status_valid>maxStat){
				maxStat = it.status_valid
			}
			out.append("\n")
		}

		if (maxStat==0){
			return new NagiosMessage(status:maxStat,text:"OK");
		} else {
			return new NagiosMessage(status:maxStat,text:out.toString());
		}

	}

}
