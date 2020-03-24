package de.unibayreuth.bayceer.bayeos.gateway.event;

import java.util.Date;


public class Event {
	
	Long id;
	Date time;
	EventType type;
	
	public Event() {
	}
	
	
	
	public Event(Long id, Date time, EventType type) {
		super();
		this.id = id;	
		this.time = time;
		this.type = type;
	}


	public Event(Long id, EventType type) {
		this(id,new java.util.Date(),type);
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}
}


