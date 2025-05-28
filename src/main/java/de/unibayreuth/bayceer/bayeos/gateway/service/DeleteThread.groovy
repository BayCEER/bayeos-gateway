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
import org.slf4j.Logger
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
class DeleteThread implements Runnable {

	@Autowired
	private DataSource dataSource

	@Value('${DELETE_OBS_RETENTION:60 days}')
	private String obsRetention;
    
    @Value('${DELETE_MSG_RETENTION:60 days}')
    private String msgRetention;
    
    @Value('${DELETE_TEMPLATE_RETENTION:1 years}')
    private String templateRetention;
    
    @Value('${DELETE_BOARD_RETENTION:1 years}')
    private String boardRetention;
        
    @Value('${DELETE_BOARD_GROUP_RETENTION:1 years}')
    private String boardGroupRetention;
    
    @Value('${DELETE_WAIT_SECS:120}')
	private int waitSecs;
	
	@Autowired
	private FrameService frameService
	
	private Logger log = LoggerFactory.getLogger(DeleteThread.class)

	@Override
	public void run() {
        Thread.sleep(1000*waitSecs);        
		while(true){
			def exit = -1
			def start = new Date()
			def obs = 0
			def obs_exported = 0
			def msg = 0
            def temps = 0
            def boards = 0
            def groups = 0						 						
			try {
				log.info("DeleteThread running")
				def db = new Sql(dataSource)
				
				try {
					log.info("Deleting observations older than ${obsRetention}.")
					db.execute("delete from observation where insert_time < (now() - ?::interval)",[obsRetention])
					obs = db.updateCount

					log.info("Deleting cached observations older than ${obsRetention}.")
					db.execute("delete from observation_exp where insert_time < (now() - ?::interval)",[obsRetention])
					obs_exported = db.updateCount
										
					log.info("Deleting messages older than ${msgRetention}.")
					db.execute("delete from message where insert_time < (now() - ?::interval)",[msgRetention])
					msg = db.updateCount
                    
                    log.info("Deleting not applied templates since ${templateRetention}.")
                    db.execute("delete from board_template where last_applied < (now() - ?::interval) or (last_applied is null and date_created < (now() - ?::interval))",[templateRetention,templateRetention])
                    temps = db.updateCount
                    
                    log.info("Deleting inactive boards since ${boardRetention}.")
                    db.execute("delete from board where last_insert_time < (now() - ?::interval) or (last_insert_time is null and date_created < (now() - ?::interval))",[boardRetention,boardRetention])
                    boards = db.updateCount
                    
                    log.info("Deleting empty board groups since ${boardGroupRetention}.")                                                                                
                    db.execute("delete from board_group where id not in (select board_group_id from board group by board_group_id) and date_created < (now() - ?::interval)",[boardGroupRetention])
                    groups = db.updateCount                                        
															
					exit = 0
				} catch (SQLException e){
					log.error(e.getMessage())
					exit = -1
				} finally {
					db.close()
					log.info("DeleteThread finished")
				}
				def millis = (new Date()).getTime() - start.getTime()
                def jsonMsg = "{'exit':${exit},'obs':${obs},'obs_exported':${obs_exported},'msg':${msg},'templates':${temps},'boards':${boards},'groups':${groups},'millis':${millis}}".toString()
				frameService.saveFrame("\$SYS/DeleteThread",new LabeledFrame(NumberType.Float32,jsonMsg))
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
