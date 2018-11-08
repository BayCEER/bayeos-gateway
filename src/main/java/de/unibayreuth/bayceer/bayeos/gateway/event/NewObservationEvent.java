package de.unibayreuth.bayceer.bayeos.gateway.event;


public class NewObservationEvent extends FrameEvent {
	
	private Long counts;
	private String origin;

	public NewObservationEvent(Long id, String origin, Long counts) {
		super(id,FrameEventType.NEW_OBSERVATION);		
		this.counts = counts;
		this.origin = origin;
	}

	public Long getCounts() {
		return counts;
	}

	public void setCounts(Long counts) {
		this.counts = counts;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
}
