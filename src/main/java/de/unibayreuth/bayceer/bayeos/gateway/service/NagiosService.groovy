package de.unibayreuth.bayceer.bayeos.gateway.service

import java.sql.SQLException
import java.util.List

import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import de.unibayreuth.bayceer.bayeos.gateway.model.NagiosMessage
import groovy.sql.Sql

@Service
class NagiosService {

	@Autowired
	DataSource dataSource


	Logger log = Logger.getLogger(NagiosService.class)


	NagiosMessage msgGateway() {
		log.info("Query status of gateway")
		def db = new Sql(dataSource)
		NagiosMessage m = getNagiosMsg(db.rows("select * from nagios_status order by group_name, board_origin, channel_nr"))
		db.close()
		return m;
	}

	NagiosMessage msgGroup(String name) {
		log.info("Query status of group: ${name}")
		def db = new Sql(dataSource)
		if (db.rows("select true from board_group where name like ?",[name]).empty ){
			db.close();
			return new NagiosMessage(status:3,text:"Group ${name} not found");
		} else {
			List channels = db.rows("select * from nagios_status where group_name like ? order by board_origin, channel_nr",[name])
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
			List channels = db.rows("select * from nagios_status where board_id=? order by channel_nr",[id]);
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
							
			def stat = db.firstRow("""
				select id, start_time, end_time, coalesce(exported,0) as exported,status, extract(EPOCH FROM end_time - start_time) as runtime,  
				round(coalesce(exported,0)/extract(EPOCH FROM end_time - start_time)) as rps
				from export_job_stat where end_time is not null and now() - end_time < ('1 hour'::interval) order by id desc limit 1
				""")						 
			if (stat != null){
					// In time
					if (stat.status == 0){
						// Finished job
						return new NagiosMessage(text:"Job finished: runtime: ${stat.runtime} [sec] records:${stat.exported} rate: ${stat.rps} [rps]|time=${stat.runtime}ms recs=${stat.exported} rate=${stat.rps}" , status:0)
					} else {
						// Critical job
						return new NagiosMessage(text:"Job failed with error.", status:2)
					}
			} else {
					// 	Unknown
					return new NagiosMessage(text:"No Job status found.", status:3)
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
		def groupName
		def boardOrigin
		channels.each{
			if (it.group_name != groupName){
				out.append("Group[${it.group_name}]\n")
				groupName = it.group_name
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
