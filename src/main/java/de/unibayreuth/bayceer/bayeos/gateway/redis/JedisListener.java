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
import redis.clients.jedis.exceptions.JedisException;

@Component
public class JedisListener implements FrameEventListener {
	
	@Autowired
	private Jedis jedis;
	
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
			try {				
				NewObservationEvent o = (NewObservationEvent) e;
				if (!o.getOrigin().startsWith("$SYS")) {									
					LocalDateTime d = LocalDateTime.now();										
					Long counts = o.getCounts();
					jedis.sadd("gateways", hostname);
					String key = "gateway:" + hostname;					
					updateHash(key,"y",d.getYear(),counts);
					updateHash(key,"m",d.getMonthValue(),counts);					
					updateHash(key,"d",d.getDayOfMonth(),counts);
					updateHash(key,"h",d.getHour(),counts);					
				}
				
			} catch (JedisException ex) {
				throw new IOException(ex.getMessage());
			}
			
		}
		

	}


	private void updateHash(String key, String field, int value, Long counts) {
				String ct = jedis.hget(key, field);
				String fieldC = field + ":c";
				if (ct == null || Long.valueOf(ct) < value) {
					jedis.hset(key,field,Long.toString(value));
					jedis.hset(key,fieldC,Long.toString(counts));
				} else {
					jedis.hincrBy(key,fieldC, counts);					
				}
						
								
				
	
	}

}
