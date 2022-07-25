package de.unibayreuth.bayceer.bayeos.gateway.event;

public class NewCommandEvent extends Event {
	private Short kind;
	private String value;
	private String description;
	
	public NewCommandEvent(Long id, Short kind, String value, String description) {
		super(id,EventType.NEW_COMMAND);
		this.kind = kind;
		this.value = value;
		this.description = description;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
