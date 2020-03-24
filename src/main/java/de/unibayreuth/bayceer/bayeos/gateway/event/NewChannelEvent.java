package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewChannelEvent extends Event {
	
	public NewChannelEvent(Long id) {
		super(id,EventType.NEW_CHANNEL);		
	}
	
}
