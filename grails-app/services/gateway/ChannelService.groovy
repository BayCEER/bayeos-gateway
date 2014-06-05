package gateway;

import groovy.sql.Sql

public class ChannelService {
	
	def dataSource
	
	
	def getChannelStatus(Channel c) {
		def db = new Sql(dataSource)
		def channelStatus = db.firstRow("SELECT status_valid, status_valid_msg, status_complete, status_complete_msg, last_result_time FROM channel_status where id = ?", c.id)
		db.close()
		return channelStatus
		
	}
}
