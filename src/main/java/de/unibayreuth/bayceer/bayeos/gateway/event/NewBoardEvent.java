package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewBoardEvent extends FrameEvent {
	

	public NewBoardEvent(Long id) {
		super(id,FrameEventType.NEW_BOARD);
	}
	
}
