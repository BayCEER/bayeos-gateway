package de.unibayreuth.bayceer.bayeos.gateway.service

import bayeos.frame.types.LabeledFrame
import groovy.sql.Sql
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.sql.DataSource
import bayeos.frame.types.NumberType


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory;


@Component
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
			def start = new Date()
			def obs = 0
			def obs_exported = 0
			def msg = 0		
				 
						
			try {
				log.info("DeleteJob running")
				def db = new Sql(dataSource)
				
				try {
					log.info("Deleting observations older than ${retention}.")
					db.execute("delete from observation where insert_time < now() - ?::interval",[retention])
					obs = db.updateCount

					log.info("Deleting exported observations older than ${retention}.")
					db.execute("delete from observation_exp where insert_time < now() - ?::interval",[retention])
					obs_exported = db.updateCount
										
					log.info("Deleting messages older than ${retention}.")
					db.execute("delete from message where insert_time < now() - ?::interval",[retention])
					msg = db.updateCount
					
										 
					exit = 0
				} catch (SQLException e){
					log.error(e.getMessage())
					exit = -1
				} finally {
					db.close()
					log.info("DeleteJob finished")
				}
				def millis = (new Date()).getTime() - start.getTime()
				frameService.saveFrame("\$SYS/DeleteJob",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'obs':${obs},'obs_exported':${obs_exported},'msg':${msg},'millis':${millis}}".toString()))
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
