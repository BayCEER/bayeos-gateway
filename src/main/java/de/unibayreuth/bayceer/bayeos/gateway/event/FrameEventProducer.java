package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class FrameEventProducer implements Runnable {
	
	private boolean stopped = false;
	private static final int QUEUE_SIZE = 200;	
	private Logger log = Logger.getLogger(FrameEventProducer.class);	
	private Set<FrameEventListener> listeners = Collections.synchronizedSet(new HashSet<FrameEventListener>());
	private LinkedBlockingDeque<FrameEvent> queue;
	
	public FrameEventProducer() {
		queue = new LinkedBlockingDeque<>(QUEUE_SIZE);
	}
	
		
	public void addFrameEvent(FrameEvent e){	
		if (!queue.offer(e)){
			log.debug("Failed to add event to queue.");
		};
	}
	
	public void addListener(FrameEventListener l) {
		listeners.add(l);
	}
	
	public void removeListener(FrameEventListener l) {
		listeners.remove(l);
	}
	

	@Override
	public void run() {
		while(!stopped){
			try {
				FrameEvent e =queue.takeFirst();
				for(FrameEventListener l:listeners) {					 
					try {
						l.eventFired(e);
					} catch (IOException ex) {
						log.error(ex.getMessage());
					}	
				}
				
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
