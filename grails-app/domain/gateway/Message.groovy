package gateway


class Message {
	
	String origin		
	String type
	String content	
	Date result_time
	
	
	static constraints = {
	
		origin(nullable:false,editable:false,unique:true)
	}
	
	static mapping = {
		version false				
		origin(sqlType:"text", index: 'message_origin_idx')					
	 }
		

    
}
