package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewMessageEvent extends FrameEvent {
	
	public NewMessageEvent(Long id) {
		super(id,FrameEventType.NEW_CHANNEL);		
	}
	
}
