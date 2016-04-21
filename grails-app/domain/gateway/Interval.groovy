package gateway

import javax.persistence.Transient;

class Interval {
    
    String name
	
	
	public Interval(String name) {
		this.name = name
	}
    
    static mapping = {        
        version false
		sort "name"
		
    }
	
	
	
	
		
    static constraints = {
								
         name(blank:false, nullable:false, validator: { value ->
			 				 
		 			 			
			 String[] p = ((String)value).split()
			 if (p.size()!=2) {
				 return "interval.incomplete"				 
			 } 
			 try {
				 Integer.parseInt(p[0])
			 } catch (NumberFormatException e)	{
			 	return "interval.invalid.quantity"
			 }
			 
			 def List<String> u = ['microsecond', 'millisecond', 'second','sec','min', 'minute', 'hour', 'day', 'week', 'month', 'year', 'microseconds', 'milliseconds', 'seconds','secs','mins', 
				 'minutes', 'hours', 'days', 'weeks', 'months', 'years', 'sec', 'min', 'h', 'd', 'w', 'm', 'y']			 			 			 			
			 if (!(u.contains(p[1]))) {				 			 
			 	return "interval.invalid.unit"
			 }
			 return true			 			 			 
         })
    }
    
    
    String toString(){
        return name;
    }
	
	
	
}
