package de.unibayreuth.bayceer.bayeos.gateway.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.unibayreuth.bayceer.bayeos.gateway.redis.JedisListener;
import de.unibayreuth.bayceer.bayeos.gateway.websocket.WebSocketListener;

@Configuration
public class EventConfig {
	
	@Autowired
	WebSocketListener ws;
	
	@Autowired
	JedisListener js;	
	
	@Value("${REDIS_EVENTS:false}")
	private Boolean redis_events;
	
	@Bean 
	public EventProducer eventProducer() {		
		EventProducer p = new EventProducer();		
		p.addListener(ws);
		
		if (redis_events) {
			p.addListener(js);	
		}
		
		return p;
	}

}
