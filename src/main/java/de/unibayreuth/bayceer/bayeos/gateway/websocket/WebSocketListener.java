package de.unibayreuth.bayceer.bayeos.gateway.websocket;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import de.unibayreuth.bayceer.bayeos.gateway.event.Event;
import de.unibayreuth.bayceer.bayeos.gateway.event.EventListener;

@Component
public class WebSocketListener implements EventListener {
	private static final String TOPIC_FRAME_EVENTS = "/topic/frameEvents";
	
	@Autowired
	private SimpMessagingTemplate template;
		
	@Override
	public void eventFired(Event e) throws IOException {
		try {
			template.convertAndSend(TOPIC_FRAME_EVENTS,e);
		} catch (MessagingException ex) {
			throw new IOException(ex.getMessage());
		}
	}

}
