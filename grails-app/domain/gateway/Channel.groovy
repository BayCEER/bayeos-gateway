package gateway

import java.util.Date;

class Channel extends CheckDevice {
	
	Integer nr
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
		
	Integer statusComplete
	String statusCompleteMsg
	
	Integer statusValid
	String statusValidMsg
	
	Board board
				

    static constraints = {
		nr(min:0)
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

		// Values populated by functions
		statusComplete(editable:false,nullable:true)
		statusCompleteMsg(editable:false,nullable:true)
		
		// Values populated by trigger
		statusValid(editable:false,nullable:true)
		statusValidMsg(editable:false,nullable:true)
    }
}
