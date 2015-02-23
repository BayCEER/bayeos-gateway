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

	
	/* Returns the speed in recs/sec
     * runTime: duration in millis
     * recs:	number of records
     * 
     */
	private int getSpeed(Long runtime, Integer recs){		
		if (recs == null || runtime == null || runtime == 0) {
			return 0
		} else {
			return Math.round((double)recs/runtime*1000)
		}															
	}
	
	NagiosMessage msgExporter() {
		log.info("Query status of export job")
		def db = new Sql(dataSource)
		def conf = db.firstRow('select * from export_job_config')
		def stat = db.firstRow('select * from export_job_stat order by id desc limit 1')
		db.close()

		if (!conf.enabled){
			return new NagiosMessage(text:"Job is disabled.", status:ReturnCode.CRITICAL.getValue())
		}
		
		int rps = 0
		long runtime = 0
		if (stat != null){						  									
			if (stat.end_time == null){
				// Running job
				runtime = (new Date().getTime() - stat.start_time.getTime())
				rps = getSpeed(runtime, stat.exported)								
				if (rps < 100) {
					// Running Job too slow					
					return new NagiosMessage(text:"Rate to low: rate: ${rps} [rps], time: ${runtime/1000} [sec]", status:ReturnCode.WARN.getValue())
				} else {
					// Running Job in time
					return new NagiosMessage(text:"Job is running: rate: ${rps} [rps], time: ${runtime/1000} [sec]", status:ReturnCode.OK.getValue())
				}
			} else {
				// Finished Job
				runtime = (stat.end_time.getTime() - stat.start_time.getTime())
				rps = getSpeed(runtime, stat.exported)
				
				int finished = (new Date().getTime() - stat.end_time.getTime())/1000				
				if (finished > 2*60*conf.sleep_interval){
					// Too old
					return new NagiosMessage(text:"Job is not running.", status:ReturnCode.CRITICAL.getValue())
				} else {
					// In time
					if (stat.status == 0){
						// Finished job
						return new NagiosMessage(text:"Job exported ${stat.exported} records: rate: ${rps} [rps], time: ${runtime/1000} [sec]", status:ReturnCode.OK.getValue())
					} else {
						// Cancelled job
						return new NagiosMessage(text:"Job failed with error.", status:ReturnCode.CRITICAL.getValue())
					}
				}
			}

		} else {
			// 	kein Eintrag -> CRITICAL
			return new NagiosMessage(text:"Job is not running.", status:ReturnCode.CRITICAL.getValue())
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
