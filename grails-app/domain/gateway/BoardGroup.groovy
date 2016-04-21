package gateway;

public class BoardGroup {
	
	String name
	
	static constraints = {
		name (unique:true, nullable:false, blank:false)		
	}
	
	String toString(){
		return name;
	}
	
	static mapping  = {
		sort "name"
	}

}
