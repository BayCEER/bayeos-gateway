package gateway;

public class BoardGroup {
	
	String name
	
	Integer dbFolderId 
	
	static constraints = {
		name (unique:true, nullable:false, blank:false)
		dbFolderId(nullable:true, min:0)
	}
	
	String toString(){
		return name;
	}
	
	static mapping  = {
		sort "name"
	}

}
