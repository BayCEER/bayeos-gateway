package gateway 

import groovy.sql.Sql
import java.sql.SQLException





public class CalculateObsJob implements Runnable {
	

	private dataSource	
	
	private int waitSecs = 120;

	public CalculateObsJob(dataSource){
		this.dataSource = dataSource		
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
