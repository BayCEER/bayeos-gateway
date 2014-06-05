package gateway

class Spline {    
    String name
    SortedSet knotePoints
	         
    static hasMany = [knotePoints:KnotPoint]  
        
    static mapping = {       
       name sqlType:"text"       
	   knotePoints sort: "x"
    }
	
	static constraints = {
		name(nullable:false)
		knotePoints()
	}
	
	
           
    String toString(){
        return name;
    }
	
	
	
        
    
    
}
