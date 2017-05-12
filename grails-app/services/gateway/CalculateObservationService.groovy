package gateway

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import gateway.CalculateObsJob
import groovy.sql.Sql

class CalculateObservationService {
	
			
	def dataSource		 
	private Thread t
	
    Boolean start() {
		log.info("Starting calculation service")		
		if (!started()) {
			Runnable task = new CalculateObsJob(dataSource)
			t = new Thread(task)
			t.start()
		}					
		log.info("Calculate service started")
		return true
    }
	
	Boolean stop() {		
		if (started()) {
			log.info("Stopping calculate service")
			t.interrupt()
			try {
				t.join()
			} catch(InterruptedException e) {
			  	log.error(e.getMessage())			    
			}
			t = null;
			log.info("Calculate service stopped")						
		}	
		return true
		
	}
	
	Boolean restart() {
		if (started() && !stop()) {			
			return false			
		} else {
		  return start()
		}
	}
	
	Boolean started(){
		return (t!=null&&t.isAlive())
	}
	
}
	
