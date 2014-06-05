package gateway

class Unit {
    String name
    String abbrevation
	Integer dbUnitId
    
   static constraints = {
        name(blank:false,unique:true)
        abbrevation(nullable:true)
		dbUnitId(nullable:true, min:0)
   }
        
    
    String toString(){
        return name;
    }
}
