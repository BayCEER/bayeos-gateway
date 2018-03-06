package de.unibayreuth.bayceer.bayeos.gateway.model;

public class NagiosMessage {	
	Integer status;
	String text;
	
	
	
	public NagiosMessage() {
		super();		
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
