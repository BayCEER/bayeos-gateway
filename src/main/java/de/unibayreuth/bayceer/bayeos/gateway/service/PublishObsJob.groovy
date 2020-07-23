package de.unibayreuth.bayceer.bayeos.gateway.service

import bayeos.frame.types.LabeledFrame
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisException
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import javax.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.sql.DataSource
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Value
import bayeos.frame.types.NumberType



@Component
class PublishObsJob implements Runnable  {

	private Logger log = LoggerFactory.getLogger(PublishObsJob.class)

	@Autowired(required = false)
	private RedisClient redis

	@Autowired
	private DataSource dataSource
	
	@Autowired
	private FrameService frameService

	private String hostname
	private String streamKey
	

	@Value('${PUBLISH_WAIT_SECS:120}')
	private int waitSecs

	private Sql db
	
	public PublishObsJob() {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "localhost";
		}
		streamKey = hostname + ":obs"
		
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000*waitSecs);
			while(true){
				def recPublished = 0
				def recCached = 0
				def exit = -1
				def start = new Date()
				StatefulRedisConnection<String, String> recon
				def db = new Sql(dataSource)
				
				try {
					log.info("PublishObsJob running")
					def id = db.firstRow("select max(id) from observation")
					if (id == null){
						log.info("Nothing to publish");
						exit = 0
					} else {
						recon = redis.connect()
						db.eachRow("SELECT channel_id, result_time, result_value FROM get_bayeos_obs(?) order by result_time asc;",[start.toTimestamp()]){r ->
							RedisCommands<String, String> cmd = recon.sync()
							Map<String, String> msg = new HashMap<>();
							msg.put('channel_id',String.valueOf(r.channel_id))
							msg.put('result_time',String.valueOf(r.result_time.getTime()))
							msg.put('result_value',String.valueOf(r.result_value))
							cmd.xadd(streamKey,msg);
							recPublished++
						}
						log.info("${recPublished} records published")
						if (recPublished> 0) {
							log.info("Move records to observation cache.")
							recCached = db.call("{call delete_obs(?,?)}",[start.toTimestamp(), id.max])
							exit = 0
						} else {
							exit = -1
						}
					}
				} catch (Exception e){
					log.error(e.getMessage())
					exit = -1
				}  finally {
					db?.close()
					recon?.close()
				}
				log.info("PublishObsJob finished")
				Thread.sleep(1000*waitSecs)
			}
		} catch (InterruptedException e){
			log.error(e.getMessage())
		}
	}



	@PostConstruct
	public void start(){
		if (redis != null) {
			new Thread(this).start()
			log.info("PublishJob started")
		}
	}
}

