package gateway


class Message {
	
	String origin		
	String type
	String content	
	Date result_time
	Date insert_time
	
	public void setContent(String value){
		// Hotfix 1.9.12
		// replace all null chars with space 
		this.content = value.replace((char)0x00,(char)0x20).trim()
	}
	
	static constraints = {
	
		origin(nullable:false,editable:false,unique:true)
	}
	
	static mapping = {
		version false				
		origin(sqlType:"text", index: 'message_origin_idx')					
	 }
		

    
}
