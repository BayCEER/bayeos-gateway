package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.sql.SQLException

import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import groovy.sql.Sql

@Component
@Profile("default")
public class CalculateObsJob implements Runnable {
	
	@Autowired
	private DataSource dataSource	
		
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
		 try {		
			def db = new Sql(dataSource)
			try {		
				log.info("CalculateJob running")			
				def ts = new Date().toTimestamp()
				def id = db.firstRow("select max(id) from observation")
				if (id == null){
					log.info("Nothing to calculate");					
				} else {					
					def rowCount = db.executeUpdate('insert into observation_calc (db_series_id, result_time, result_value) select db_series_id, result_time, result_value from get_bayeos_obs(?)',[ts])
					log.info("${rowCount} observations calculated")
					
					log.info("Move records to archive table.")
					db.call("{call delete_obs(?,?)}",[ts,id.max])
				}
			} catch (SQLException e){
				log.error(e.getMessage())	
			}  finally {
				db.close()
				log.info("CalculateJob finished")
			}
			Thread.sleep(1000*waitSecs);
		 } catch (InterruptedException e){
			 break;
		 }
		}					
	}

}
