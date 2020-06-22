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
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

@Component
public class RedisObsPerHour implements EventListener {
	
	@Autowired
	private RedisClient redis;
	
	private String hostname;
	
	public RedisObsPerHour() {		
		try {	
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "";
		}		
	}
			
	 
	@Override
	public void eventFired(Event e) throws IOException {
		switch (e.getType()) {
		case NEW_OBSERVATION :	
			try (StatefulRedisConnection<String, String> con = redis.connect()){
				RedisCommands<String, String> cmd = con.sync();
				cmd.sadd("hostnames", hostname);
				NewObservationEvent o = (NewObservationEvent) e;
				if (!o.getOrigin().startsWith("$SYS")) {					
					String key = String.format("%s:obs", hostname);	
					// Update cached values 
					// z-ordered set: score: ts , value: counts:ts as string 
					// value must be unique!
					ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());					
					cmd.zrangebyscore(key, 0, now.minusHours(1).toInstant().toEpochMilli());										
					cmd.zadd(key,now.toInstant().toEpochMilli(), o.getCounts() + ":" + now.toInstant().toEpochMilli());										
					// Calculate rate for last hour 
					long sum = 0;
					for(String value:cmd.zrange(key, 0, -1)) {
						sum+= Long.valueOf(value.split(":")[0]);						
					}				
					cmd.hset(hostname + ":stats", "obs_per_hour",String.valueOf(sum));										
				}
				
			} catch (RedisException ex) {
				throw new IOException(ex.getMessage());
			}	
			break;
		default:
			break;
		}
		
		
	}


	
	
	
	
}
