package gateway;

public class ExportJobConfig {
	
		def springSecurityService
	
		URL url = new URL("http://localhost/BayEOS-Server/XMLServlet")
		String userName = "import"
		String password = ""
		
		Integer dbHomeFolderId
		Integer dbHomeUnitId
		
		Integer sleepInterval = 10 
		Integer recordsPerBulk = 10000 		
		Boolean enabled	= true	
		
		static constraints = {
			password nullable:true, blank:true
			dbHomeFolderId nullable:true
			dbHomeUnitId nullable:true 			 
			userName nullable:true, blank:true
		}
		
		def beforeInsert() {			
				encodeValue()						
		}
		
		def beforeUpdate() {
			if (isDirty('password')) {
				encodeValue()
			}
		}
	
		protected void encodeValue() {
			if (!password.isEmpty()){
				password = springSecurityService.encodePassword(password)
			}
	
		}

}
