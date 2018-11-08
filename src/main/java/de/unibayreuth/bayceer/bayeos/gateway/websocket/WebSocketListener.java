package de.unibayreuth.bayceer.bayeos.gateway.websocket;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEvent;
import de.unibayreuth.bayceer.bayeos.gateway.event.FrameEventListener;

@Component
public class WebSocketListener implements FrameEventListener {
	private static final String TOPIC_FRAME_EVENTS = "/topic/frameEvents";
	
	@Autowired
	private SimpMessagingTemplate template;
		
	@Override
	public void eventFired(FrameEvent e) throws IOException {
		try {
			template.convertAndSend(TOPIC_FRAME_EVENTS,e);
		} catch (MessagingException ex) {
			throw new IOException(ex.getMessage());
		}
	}

}
