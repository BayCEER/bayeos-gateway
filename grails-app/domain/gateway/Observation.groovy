package gateway

class Observation {

	Integer channelId			
    Date resultTime
    Float resultValue
	Integer statusValid			                    
                    
    
    static mapping = {
		'id'(type:org.hibernate.type.LongType, class: Long)
		'channel_id' (type:org.hibernate.type.LongType, class: Long)
	    resultTime(sqlType:"timestamp with time zone")												
        version false
    }
    
       
    
}


