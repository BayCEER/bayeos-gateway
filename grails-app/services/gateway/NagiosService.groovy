package gateway

import groovy.sql.DataSet;
import groovy.sql.Sql

class NagiosService {
	
	def dataSource

	static transactional = false
		

    NagiosMessage msgGateway() {		
		log.info("Query status of gateway")
		def db = new Sql(dataSource)	
		NagiosMessage m = getNagiosMsg(db.rows("select * from nagios_status order by group_name, board_origin, channel_nr"))
		db.close()
		return m;
    }
	
	NagiosMessage msgGroup(String name) {
		log.info("Query status of group: $name")
		def db = new Sql(dataSource)						
		if (db.rows("select true from board_group where name like '$name'").empty ){
			db.close();
			return new NagiosMessage(status:3,text:"Group $name not found");				
		} else {
			List channels = db.rows("select * from nagios_status where group_name like '$name' order by board_origin, channel_nr")
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
			return new NagiosMessage(status:3,text:"Board with id:$id not found");
		} else {		
			List channels = db.rows("select * from nagios_status where board_id=? order by channel_nr",[id]);
			db.close()
			NagiosMessage m = getNagiosMsg(channels)			
			return m;
		}							
	}
	
		
	NagiosMessage msgChannel(Integer id) {
		log.info("Query status of channel: $id")
		def db = new Sql(dataSource)		
		if (db.rows("select true from channel where id=?",[id]).empty){
			db.close();
			return new NagiosMessage(status:3,text:"Channel with id:$id not found");
		} else {
			List channels = db.rows('select * from nagios_status where channel_id=?',[id])
			db.close()
			NagiosMessage m = getNagiosMsg(channels)			
			return m;
		}		
	}
	
	
	NagiosMessage msgExporter() {
		log.info("Query status of exporter")
		def db = new Sql(dataSource)
		def row = db.firstRow """select case when new.c != 0 then round(exp.c*100/(new.c+exp.c),2) else 100 end as enr, new.c as new, exp.c as exp  from
					(select count(*)::numeric as c from observation_exp where result_time > now() - '14 days'::interval) as exp,
					(select count(*)::numeric as c from observation, channel  where result_time > now() - '14 days'::interval and observation.channel_id = channel.id and channel.db_series_id is not null) as new"""		
		db.close()
		NagiosMessage msg = new NagiosMessage(text:"enr:${row.enr} new:${row.new} exported:${row.exp}", status:ReturnCode.CRITICAL.getValue())
		if (row.enr >= 90){
			msg.status = ReturnCode.OK.getValue()
		} else if (row.enr >= 70){
			msg.status = ReturnCode.WARN.getValue()
		}
		return msg
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
