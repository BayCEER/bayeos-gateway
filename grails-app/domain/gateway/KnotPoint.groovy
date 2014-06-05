package gateway

class KnotPoint implements Comparable {    
    Float x
    Float y
        
    static belongsTo = [spline:Spline]
    
    static mapping = {      
        x index: 'knote_point_idx'
        id index: 'knote_point_idx'		
    }
        
    int compareTo(obj) {
       x.compareTo(obj.x)
    }
	
	String toString() {
		return "${x},${y}"	
	};
       
    
}
