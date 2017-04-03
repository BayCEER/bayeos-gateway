package de.unibayreuth.bayceer.bayeos.gateway.websocket;

import java.util.Date;


public class FrameEvent {
	
	String origin;
	Date time;
	FrameEventType type;
	
	public FrameEvent() {
	}
	
	
	
	public FrameEvent(String origin, Date time, FrameEventType type) {
		super();
		this.origin = origin;
		this.time = time;
		this.type = type;
	}


	public FrameEvent(String origin, FrameEventType type) {
		this(origin,new java.util.Date(),type);
	}
	
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
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
}


