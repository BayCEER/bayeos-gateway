package de.unibayreuth.bayceer.bayeos.gateway.websocket;

import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class FrameEventProducer implements Runnable {
	private static final String TOPIC_FRAME_EVENTS = "/topic/frameEvents";
	private static final int QUEUE_SIZE = 100;
	boolean stopped = false;
	
	private Logger log = Logger.getLogger(FrameEventProducer.class);	
	
	@Autowired
	private SimpMessagingTemplate template;
	private LinkedBlockingDeque<FrameEvent> queue;

	
	public FrameEventProducer() {
		queue = new LinkedBlockingDeque<>(QUEUE_SIZE);
	}
	
		
	public void addFrameEvent(FrameEvent e){
		
		if (!queue.offer(e)){
			log.debug("Failed to add event to queue.");
		};
	}
	
	private void fireEvent(FrameEvent e){
		template.convertAndSend(TOPIC_FRAME_EVENTS,e);
	}

	@Override
	public void run() {
		while(!stopped){
			try {
				fireEvent(queue.takeFirst());
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	@PostConstruct
	public void start(){
		new Thread(this).start();
	}
}
