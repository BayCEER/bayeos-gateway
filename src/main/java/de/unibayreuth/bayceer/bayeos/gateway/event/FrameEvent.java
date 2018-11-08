package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.util.Date;


public class FrameEvent {
	
	Long id;
	Date time;
	FrameEventType type;
	
	public FrameEvent() {
	}
	
	
	
	public FrameEvent(Long id, Date time, FrameEventType type) {
		super();
		this.id = id;	
		this.time = time;
		this.type = type;
	}


	public FrameEvent(Long id, FrameEventType type) {
		this(id,new java.util.Date(),type);
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public FrameEventType getType() {
		return type;
	}
	public void setType(FrameEventType type) {
		this.type = type;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}
}


