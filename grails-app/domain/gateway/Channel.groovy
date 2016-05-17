package gateway

import java.util.Date;

class Channel extends CheckDevice implements Comparable<Channel>{
	
	String nr
	String label
	String phenomena
	
	/* Export to BayEOS Database Configuration */
	Integer dbSeriesId
	Boolean dbExcludeAutoExport
	
	
	Spline spline
	Unit unit
	
	Interval aggrInterval
	Function aggrFunction
		 	
	Date lastResultTime		
	Float lastResultValue
	
	Integer lastCount
		
	
	Integer statusValid
	String statusValidMsg
	
	Board board
				

    static constraints = {
		nr(nullable:false)
		label(nullable:true)
		phenomena(nullable:true)
		unit(nullable:true)
		spline(nullable:true)
		aggrInterval(nullable:true, validator: { val, obj ->			
			(val == null) == (obj.aggrFunction == null)
		})
		aggrFunction(nullable:true, validator: { val, obj ->			
			(val == null) == (obj.aggrInterval == null)
		})
		dbSeriesId(nullable:true, min:0)
		dbExcludeAutoExport(defaultValue:false)
		lastResultTime(nullable:true, editable:false)	
		lastResultValue(nullable:true, editable:false)
		lastCount(nullable:true, editable:false)			
		statusValid(editable:false,nullable:true)
		statusValidMsg(editable:false,nullable:true)
    }



	@Override
	public int compareTo(Channel o) {
		if (nr.isInteger() && o.nr.isInteger()){
			return Integer.valueOf(nr).compareTo(Integer.valueOf(o.nr))
		} else {
			return nr.compareTo(o.nr)
		}
		
	}}
