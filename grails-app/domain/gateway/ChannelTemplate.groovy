package gateway

import java.util.List;

class ChannelTemplate extends CheckDevice implements Comparable<ChannelTemplate>{
	
	String nr
	String label
	String phenomena
	Unit unit
	Spline spline	
	Interval aggrInterval
	Function aggrFunction			
		
	
	// not persisted
	static transients = [ "excludeFromNagios", "filterCriticalValues"]	

	static belongsTo = [boardTemplate: BoardTemplate]
	
    static constraints = {
		nr(nullable:false)		
		label(nullable:false)
		phenomena(nullable:true)
		unit(nullable:true)
		spline(nullable:true)
		
		aggrInterval(nullable:true, validator: { val, obj ->
			(val == null) == (obj.aggrFunction == null)
		})
		aggrFunction(nullable:true, validator: { val, obj ->
			(val == null) == (obj.aggrInterval == null)
		})
				
    }
	
	
	
	def String toString() {		
			def u = unit?.name ?: "no unit"
			def p = phenomena ?: "no phenomena"					
			return "${nr} - ${label} - ${p} [${u}]"		
	}



	@Override
	public int compareTo(ChannelTemplate o) {
		if (nr.isInteger() && o.nr.isInteger()){
			return Integer.valueOf(nr).compareTo(Integer.valueOf(o.nr))
		} else {
			return nr.compareTo(o.nr)
		}		

	}


	

	
	
}
