package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventProducer implements Runnable {
	
	private boolean stopped = false;
	private static final int QUEUE_SIZE = 10000;	
	private Logger log = LoggerFactory.getLogger(EventProducer.class);	
	private Set<EventListener> listeners = Collections.synchronizedSet(new HashSet<EventListener>());
	private LinkedBlockingDeque<Event> queue;
	
	public EventProducer() {
		queue = new LinkedBlockingDeque<>(QUEUE_SIZE);
	}
			
	public void addFrameEvent(Event e){	
		if (!queue.offer(e)){
			log.debug("Failed to add event to queue.");
		};
	}
	
	public void addListener(EventListener l) {
		listeners.add(l);
	}
	
	public void removeListener(EventListener l) {
		listeners.remove(l);
	}
	

	@Override
	public void run() {
		while(!stopped){
			try {
				Event e =queue.takeFirst();
				for(EventListener l:listeners) {					 
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
