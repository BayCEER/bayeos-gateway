package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.util.Map;

public class NewFrameEvent extends Event  {
	
	private Map<String,Object> frame;
	
	public NewFrameEvent(Map<String,Object> frame) {
		this(null,frame);
	}

	public NewFrameEvent(Long id, Map<String,Object> frame) {
		super(id,EventType.NEW_FRAME);
		this.setFrame(frame);
	}

	public Map<String, Object> getFrame() {
		return frame;
	}

	public void setFrame(Map<String, Object> frame) {
		this.frame = frame;
	}

	
}
