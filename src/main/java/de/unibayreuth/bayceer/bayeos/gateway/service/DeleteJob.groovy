package de.unibayreuth.bayceer.bayeos.gateway.service

import groovy.sql.Sql
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import bayeos.frame.types.NumberType
import bayeos.frame.types.LabeledFrame


@Component
@Profile("default")
class DeleteJob implements Runnable {

	@Autowired
	private DataSource dataSource

	@Value('${DELETE_RETENTION:60 days}')
	private String retention;

	@Value('${DELETE_WAIT_SECS:120}')
	private int waitSecs;
	
	@Autowired
	private FrameService frameService
	
	private Logger log = LoggerFactory.getLogger(DeleteJob.class)

	@Override
	public void run() {
		Thread.sleep(1000*waitSecs);
		while(true){
			def exit = -1					
			try {
				log.info("DeleteJob running")
				def db = new Sql(dataSource)
				
				try {
					log.info("Deleting observations older than ${retention}.")
					db.execute("delete from observation where insert_time < now() - ?::interval",[retention])

					log.info("Deleting exported observations older than ${retention}.")
					db.execute("delete from observation_exp where insert_time < now() - ?::interval",[retention])
					
					log.info("Deleting messages older than ${retention}.")
					db.execute("delete from message where insert_time < now() - ?::interval",[retention])
					exit = 0
				} catch (SQLException e){
					log.error(e.getMessage())
					exit = -1
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
