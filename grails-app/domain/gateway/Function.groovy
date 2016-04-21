package gateway

class Function {
    
    
    String name
   
    
    static mapping = {              
       sort "name"
    }
    
    static constraint = {
         name(blank:false, nullable:false)
    }
    
    String toString(){
        return name;
    }
}
