package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewMessageEvent extends Event {
	
	public NewMessageEvent(Long id) {
		super(id,EventType.NEW_CHANNEL);		
	}
	
}
