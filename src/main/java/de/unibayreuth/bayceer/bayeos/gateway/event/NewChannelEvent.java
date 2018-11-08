package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewChannelEvent extends FrameEvent {
	
	public NewChannelEvent(Long id) {
		super(id,FrameEventType.NEW_CHANNEL);		
	}
	
}
