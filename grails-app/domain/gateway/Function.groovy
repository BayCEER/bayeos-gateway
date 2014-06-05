package gateway

class Function {
    
    
    String name
   
    
    static mapping = {              
       
    }
    
    static constraint = {
         name(blank:false, nullable:false)
    }
    
    String toString(){
        return name;
    }
}
