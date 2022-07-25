package de.unibayreuth.bayceer.bayeos.gateway.event;

public class NewCommandResponseEvent extends Event {
	private Short kind;
	private String value;
	
	public NewCommandResponseEvent(Long id,Short kind, String value) {
		super(id,EventType.NEW_COMMAND_RESPONSE);
		this.kind = kind;
		this.value = value;
	}

	public Short getKind() {
		return kind;
	}

	public void setKind(Short kind) {
		this.kind = kind;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	

}
