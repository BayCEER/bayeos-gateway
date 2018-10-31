package de.unibayreuth.bayceer.bayeos.gateway.service;

import groovy.sql.Sql
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import bayeos.frame.types.NumberType
import bayeos.frame.types.LabeledFrame


@Component
public class CalculateObsJob implements Runnable {
	
	@Autowired
	private DataSource dataSource	
	
	@Autowired
	private FrameService frameService
		
	@Value('${CALC_WAIT_SECS:120}')
	private int waitSecs
	
	private Logger log = Logger.getLogger(CalculateObsJob.class)
	
	@PostConstruct
	public void start(){
		new Thread(this).start()
	}

	@Override
	public void run() {
		while(true) {	
			
		 def exit = -1
		 def start = new Date()
		 def rowCalculated = 0
		 def rowArchived = 0
		 
		 try {		
			def db = new Sql(dataSource)
			try {		
				log.info("CalculateJob running")			
				def ts = new Date().toTimestamp()
				def id = db.firstRow("select max(id) from observation")
				if (id == null){
					log.info("Nothing to calculate");										
				} else {					
					rowCalculated = db.executeUpdate('insert into observation_calc (id, channel_id, result_time, result_value) select id, channel_id, result_time, result_value from get_bayeos_obs(?)',[ts])
					log.info("${rowCalculated} observations calculated")					
					log.info("Move records to archive table.")
					rowArchived = db.call("{call delete_obs(?,?)}",[ts,id.max])										
				}
				exit = 0
			} catch (SQLException e){
				log.error(e.getMessage())
				exit = -1	
			}  finally {
				db.close()
				log.info("CalculateJob finished")
			}
			def millis = (new Date()).getTime() - start.getTime()			
			frameService.saveFrame("\$SYS/CalculateJob",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'calculated':${rowCalculated},'archived':${rowArchived},'millis':${millis}}".toString()))
			Thread.sleep(1000*waitSecs);
		 } catch (InterruptedException e){
			 break;
		 }
		}					
	}

}
