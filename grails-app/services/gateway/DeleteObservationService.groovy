package gateway

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import gateway.DeleteJob
import groovy.sql.Sql

class DeleteObservationService {
	
			
	def dataSource		 
	private Thread t
	
    Boolean start() {
		log.info("Starting delete service")		
		if (!started()) {
			Runnable task = new DeleteJob(dataSource, DeleteJobConfig.first())
			t = new Thread(task)
			t.start()
		}					
		log.info("Delete service started")
		return true
    }
	
	Boolean stop() {		
		if (started()) {
			log.info("Stopping delete service")
			t.interrupt()
			try {
				t.join()
			} catch(InterruptedException e) {
			  	log.error(e.getMessage())			    
			}
			t = null;
			log.info("Delete service stopped")						
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
	
