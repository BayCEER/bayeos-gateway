package gateway

class FileController {
	
	def loggerService
        		
	def show() {
		String file = LogFile.getFileContents(params.file,100)
		String title = params.title				
		[level:loggerService.getRootLogger().level,file:file, title:title]
	}
}
