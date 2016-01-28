package gateway;

import groovy.sql.Sql

public class ChannelService {
	
	def dataSource
	
	
	def getChannelStatus(Channel c) {
		def db = new Sql(dataSource)
		def channelStatus = db.firstRow("SELECT ck.status_valid, ck.status_valid_msg, ck.status_complete, ck.status_complete_msg, c.last_result_time FROM channel_check ck, channel c where c.id = chk.id and id = ?", c.id)
		db.close()
		return channelStatus
		
	}
}
