package gateway


/* Global Settings for Boards and Channels */
abstract class CheckDevice {	
	// Validation Check
	Float criticalMax
	Float criticalMin
	Float warningMax
	Float warningMin	
	// Completeness Check
	Integer samplingInterval	
	// Disable Alerts   
	Boolean excludeFromNagios = false	
	// Import valid records only 
	Boolean filterCriticalValues = false
	
	
			
    static constraints = {
		criticalMax(nullable:true)
		warningMax(nullable:true)
		warningMin(nullable:true)
		criticalMin(nullable:true)
		samplingInterval(nullable:true, min:0)				
    }
	
	static mapping = {
		tablePerHierarchy false		
	}
}
