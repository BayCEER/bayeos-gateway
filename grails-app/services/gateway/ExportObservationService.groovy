package gateway

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import gateway.ExportJob
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.codehaus.groovy.grails.web.context.ServletContextHolder



class ExportObservationService {

	def dataSource	
	def grailsLinkGenerator
		
	private Thread t
		
	Boolean start() {
		log.info("Starting export service")
		if (!started()) {				
			def url = grailsLinkGenerator.getServerBaseURL() 
			def context = ServletContextHolder.getServletContext().getContextPath()
			Runnable task = new ExportJob(url, dataSource, ExportJobConfig.first())
			t = new Thread(task)
			t.start()
		}
		log.info("Export service started")
		return true						
	}
	
	Boolean restart() {
		if (started() && !stop()) {
			return false
		} else {
		  return start()
		}
	}
	
	Boolean stop() {		
		if (started()) {
			log.info("Stopping export service")
			t.interrupt()
			try {
				t.join()
			} catch(InterruptedException e) {
			  	log.error(e.getMessage())
			    return false
			}
			t = null;
			log.info("Export service stopped")			
			return true
		}		
		
	}
	
	Boolean started(){
		return (t!=null&&t.isAlive())
	}
	
	
}
