package gateway

class FileFormat {
	
	String name
	String xml
	
	
	static mapping = {
		name sqlType:"text"
		xml sqlType:"text"
		sort "name"
	 }
	 
	 static constraints = {
		 name(nullable:false)
		 xml(nullable:false)		 
	 }
			
	 String toString(){
		 return name
	 }
		 	 
}
