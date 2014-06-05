package gateway


class LoggerController {
	def loggerService
    
	def debug() { 
		loggerService.debug()		
	}
	
	def info() {
		loggerService.info()
	}
	
	def warn() {
		loggerService.warn()
	}
	
	def error() {
		loggerService.error()
	}
}
