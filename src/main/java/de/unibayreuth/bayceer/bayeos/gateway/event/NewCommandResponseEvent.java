package de.unibayreuth.bayceer.bayeos.gateway.event;

public class NewCommandResponseEvent extends Event {
	private Short key;
	private Short status;
	private String value;
	
	public NewCommandResponseEvent(Long id,Short key, Short status, String value) {
		super(id,EventType.NEW_COMMAND_RESPONSE);
		this.key = key;
		this.status = status;
		this.value = value;
	}

	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	public Short getKey() {
		return key;
	}


	public void setKey(Short key) {
		this.key = key;
	}


	public Short getStatus() {
		return status;
	}


	public void setStatus(Short status) {
		this.status = status;
	}
	
	

}
