package de.unibayreuth.bayceer.bayeos.gateway.service

import groovy.sql.Sql
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DeleteJob implements Runnable {

	@Autowired
	private DataSource dataSource

	@Value('${DELETE_RETENTION:60 days}')
	private String retention;

	@Value('${DELETE_WAIT_SECS:120}')
	private int waitSecs;
	
	private Logger log = Logger.getLogger(DeleteJob.class)

	@Override
	public void run() {

		while(true){
			try {
				log.info("DeleteJob running")
				def db = new Sql(dataSource)
				
				try {
					log.info("Deleting observations older than ${retention}.")
					db.execute("delete from observation where insert_time < now() - ?::interval",[retention])

					log.info("Deleting exorted observations older than ${retention}.")
					db.execute("delete from observation_exp where insert_time < now() - ?::interval",[retention])

					log.info("Deleting messages older than ${retention}.")
					db.execute("delete from message where insert_time < now() - ?::interval",[retention])

					log.info("Deleting statistics older than ${retention}.")
					db.execute("delete from export_job_stat where start_time < now() - ?::interval",[retention])
				} catch (SQLException e){
					log.error(e.getMessage())
				} finally {
					db.close()
					log.info("DeleteJob finished")
				}
				Thread.sleep(1000*waitSecs)
			} catch (InterruptedException e){
				break;
			}
		}
	}

	@PostConstruct
	public void start(){
		new Thread(this).start()
	}
}
