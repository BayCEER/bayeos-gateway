package de.unibayreuth.bayceer.bayeos.gateway.redis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEvent;
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEventListener;
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEventType;
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
		if (e.getType() == FrameEventType.NEW_OBSERVATION) {
			try (Jedis jedis = jedisPool.getResource()){				
				NewObservationEvent o = (NewObservationEvent) e;
				if (!o.getOrigin().startsWith("$SYS")) {									
					LocalDateTime d = LocalDateTime.now();										
					Long counts = o.getCounts();
					jedis.sadd("gateways", hostname);
					String key = "gateway:" + hostname;					
					updateHash(jedis,key,"y",d.getYear(),counts);
					updateHash(jedis, key,"m",d.getMonthValue(),counts);					
					updateHash(jedis, key,"d",d.getDayOfMonth(),counts);
					updateHash(jedis, key,"h",d.getHour(),counts);					
				}
				
			} catch (JedisException ex) {
				throw new IOException(ex.getMessage());
			}
			
		}		
	}


	private void updateHash(Jedis jedis,String key, String interval, int value, Long counts) {
				String intervalCount = interval + ":c";				
				if (jedis.hget(key,interval).equals(String.valueOf(value))) {
					jedis.hincrBy(key,intervalCount,counts);						
				} else {
					jedis.hset(key,interval,Long.toString(value));
					jedis.hset(key,intervalCount,Long.toString(counts));		
				}
	}
	
	
	
	
}
