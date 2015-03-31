package gateway

class Comment {	
	
	User user
	Date insert_time = new Date()
	String content
	

    static constraints = {
    }
	
	static mapping = {
		content sqlType:"text"			
	 }
}
