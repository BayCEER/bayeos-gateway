package gateway

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import gateway.ExportJob


class ExportObservationService {

	def dataSource
	private Thread t
	
	
	Boolean start() {
		log.info("Starting export service")
		if (!started()) {
			Runnable task = new ExportJob(dataSource, ExportJobConfig.first())
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
