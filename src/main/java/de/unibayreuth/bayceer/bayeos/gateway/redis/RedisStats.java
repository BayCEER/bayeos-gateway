package de.unibayreuth.bayceer.bayeos.gateway.redis;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.DomainRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class RedisStats implements Runnable {
	
	@Value("${REDIS_EVENTS:false}")
	private Boolean redis_events;
	
	@Value("${REDIS_STATS_WAIT_SECS:120}")
	private Integer redis_stats_wait_secs;
	
	@Autowired
	private JedisPool jedisPool;
	
	private String hostname;
	
	@Autowired
	private BoardRepository repoBoard;
	
	@Autowired
	private DomainRepository repoDomain;
	
	@Autowired
	private UserRepository repoUser;
	
	private Logger log = Logger.getLogger(RedisStats.class);
	
	
	public RedisStats() {		
		try {	
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "";
		}		
	}

	@Override
	public void run() {
		
		try {
			
			while(true){
				Thread.sleep(1000*redis_stats_wait_secs);
				log.info("Pushing stats to redis.");
				
				try (Jedis jedis = jedisPool.getResource()){	
					jedis.sadd("hostnames", hostname);
					jedis.hset(hostname + ":stats", "domains",String.valueOf(repoDomain.count()));
					jedis.hset(hostname + ":stats", "boards",String.valueOf(repoBoard.count()));
					jedis.hset(hostname + ":stats", "users",String.valueOf(repoUser.count()));
				} catch (JedisException e) {
					log.error(e.getMessage());
				}
				
			
				
			}
			
			
		} catch (InterruptedException e){
			log.error(e.getMessage());
		}
		

	}
	
	@PostConstruct
	public void start(){
		if (redis_events) {
			new Thread(this).start();	
		}		
	}

}
