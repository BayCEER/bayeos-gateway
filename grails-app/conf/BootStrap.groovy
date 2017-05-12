import gateway.DeleteJobConfig;
import gateway.ExportJobConfig;
import gateway.ExportObservationService
import gateway.DeleteObservationService
import grails.util.Environment;

import org.codehaus.groovy.grails.commons.ApplicationAttributes

class BootStrap {
	
	def deleteObservationService
	def exportObservationService
	def calculateObservationService
					
	def init = { servletContext ->
		def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
		def messageSource = ctx.getBean("messageSource")
		messageSource.fallbackToSystemLocale = false		

		// Locale.setDefault(Locale.US);
		
		if (Environment.current == Environment.PRODUCTION) {							
		 if (DeleteJobConfig.first()?.enabled) {
			  deleteObservationService.start()
		 }
		 
		 if (ExportJobConfig.first()?.enabled) {
			 exportObservationService.start()
		 }

		 calculateObservationService.start()
		}
		 
    }
    
	def destroy = {		
		if (Environment.current == Environment.PRODUCTION) {
		 deleteObservationService.stop()
		 exportObservationService.stop()
		 calculateObservationService.stop()
		}
    }
}
