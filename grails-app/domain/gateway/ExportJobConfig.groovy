package gateway;

public class ExportJobConfig {
		
	    	
		URL url = new URL("http://localhost/BayEOS-Server/XMLServlet")
		String userName = "import"
		String password = ""
		
		Integer dbHomeFolderId
		Integer dbHomeUnitId
		
		Integer sleepInterval = 10 
		Integer recordsPerBulk = 10000 		
		Boolean enabled	= true	
		
		static constraints = {
			password nullable:true, blank:true, size:0..32	
			dbHomeFolderId nullable:true
			dbHomeUnitId nullable:true 			 
			userName nullable:true, blank:true
		}
		
		static mapping = {
			password type: EncryptedString
		}
		

}
