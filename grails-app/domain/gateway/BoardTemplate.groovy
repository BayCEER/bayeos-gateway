package gateway

class BoardTemplate extends CheckDevice {	
	String name
	String description
	String revision		
	String dataSheet
	Date dateCreated
	Date lastUpdated	
		
	SortedSet channelTemplates
	
	static hasMany = [channelTemplates:ChannelTemplate]
	
	// not persisted
	static transients = [ "excludeFromNagios", "filterCriticalValues" ]

	
    static constraints = {		
		name(blank: false)
		description(nullable:true)
		revision(nullable:true)
		dataSheet(nullable:true, url:true)		
    }
					
	
	static mapping  = {
		channelTemplates sort:"nr"
		revision(sqlType:"text")
		description(sqlType:"text")
		dataSheet(sqlType:"text")	
		sort "name"		
	}
	    
	
	String toString(){
		return "${name} , rev. ${revision}"		
	}
	
	
}
