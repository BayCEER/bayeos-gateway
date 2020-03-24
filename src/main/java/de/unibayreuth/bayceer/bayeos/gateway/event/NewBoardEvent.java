package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewBoardEvent extends Event {
	

	public NewBoardEvent(Long id) {
		super(id,EventType.NEW_BOARD);
	}
	
}
