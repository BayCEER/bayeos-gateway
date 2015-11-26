package gateway

import groovy.sql.Sql
import java.util.Date;

class Board extends CheckDevice {
	
	def dataSource
	
	String origin
	String name
	
	/* Export to BayEOS Database Configuration */
	Integer dbFolderId
	Boolean dbAutoExport = false
	
	// Seconds to shift check time interval backwards
	Integer checkDelay
	
	// Generated by trigger
	Short lastRssi
	Date lastResultTime
	
	BoardGroup boardGroup
	
	static hasMany = [comments: Comment]
	
	
					
    static constraints = {					
		origin(nullable:false,editable:false,unique:true)
		name(nullable:true)
		dbFolderId(nullable:true, min:0)		
		lastRssi(editable:false, nullable:true)
		lastResultTime(editable:false,nullable:true)
		checkDelay(nullable:true, min:0)
		boardGroup(nullable:true)				
    }
	
	static mapping = {
		origin(sqlType:"text", index: 'board_origin_idx')		
		comments cascade: "all"	
	}
	
		
			
	
}
