package de.unibayreuth.bayceer.bayeos.gateway.redis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEvent;
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEventListener;
import de.unibayreuth.bayceer.bayeos.gateway.event.NewObservationEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class JedisListener implements FrameEventListener {
	
	@Autowired
	private JedisPool jedisPool;
	
	private String hostname;
	
	public JedisListener() {		
		try {	
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "";
		}		
	}
		
	 
	@Override
	public void eventFired(FrameEvent e) throws IOException {
		
		switch (e.getType()) {
		case NEW_OBSERVATION :			
			try (Jedis jedis = jedisPool.getResource()){				
				NewObservationEvent o = (NewObservationEvent) e;
				if (!o.getOrigin().startsWith("$SYS")) {					
					jedis.sadd("hostnames", hostname);					
					String key = hostname + ":obs";										
					// Update z-ordered set: score: ts , value: counts:ts as string
					ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());					
					jedis.zremrangeByScore(key, 0, now.minusHours(1).toInstant().toEpochMilli());										
					jedis.zadd(key,now.toInstant().toEpochMilli(), o.getCounts() + ":" + now.toInstant().toEpochMilli());										
					
					// Calculate rate for last hour 
					int obs_per_hour = 0;
					for(String value:jedis.zrange(key, 0, -1)) {
						obs_per_hour+= Integer.valueOf(value.split(":")[0]);						
					}					
					jedis.hset(hostname + ":stats", "obs_per_hour",String.valueOf(obs_per_hour));										
				}
				
			} catch (JedisException ex) {
				throw new IOException(ex.getMessage());
			}
			break;

		case NEW_BOARD:			
			break;
		
		case NEW_MESSAGE:			
			break;

		default:
			break;
		}
		
		
	}


	
	
	
	
}
