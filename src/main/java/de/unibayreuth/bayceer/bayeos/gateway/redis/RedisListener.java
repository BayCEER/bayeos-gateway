package de.unibayreuth.bayceer.bayeos.gateway.redis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unibayreuth.bayceer.bayeos.gateway.event.Event;
import de.unibayreuth.bayceer.bayeos.gateway.event.EventListener;
import de.unibayreuth.bayceer.bayeos.gateway.event.NewObservationEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class RedisListener implements EventListener {
	
	
	@Autowired
	private JedisPool jedisPool;
	
	private String hostname;
	
	public RedisListener() {		
		try {	
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "";
		}		
	}
	
	
		
	private Integer calcRate(Jedis jedis, String key, Integer score) throws JedisException {	
		// Update cached values 
		// z-ordered set: score: ts , value: counts:ts as string 
		// value must be unique!
		ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());					
		jedis.zremrangeByScore(key, 0, now.minusHours(1).toInstant().toEpochMilli());										
		jedis.zadd(key,now.toInstant().toEpochMilli(), score + ":" + now.toInstant().toEpochMilli());										
		
		// Calculate rate for last hour 
		int sum = 0;
		for(String value:jedis.zrange(key, 0, -1)) {
			sum+= Integer.valueOf(value.split(":")[0]);						
		}
		return sum;		
	}
		
	 
	@Override
	public void eventFired(Event e) throws IOException {
		
		switch (e.getType()) {
		case NEW_OBSERVATION :		
			try (Jedis jedis = jedisPool.getResource()){		
				jedis.sadd("hostnames", hostname);
				NewObservationEvent o = (NewObservationEvent) e;
				if (!o.getOrigin().startsWith("$SYS")) {																			 								
					jedis.hset(hostname + ":stats", "obs_per_hour",String.valueOf(calcRate(jedis, hostname + ":obs", o.getCounts().intValue())));										
				}  				
			} catch (JedisException ex) {
				throw new IOException(ex.getMessage());
			}
			break;

		case NEW_BOARD:			
			break;		
		case NEW_MESSAGE:			
			break;
		case NEW_FRAME:
			break;
		default:
			break;
		}
		
		
	}


	
	
	
	
}
