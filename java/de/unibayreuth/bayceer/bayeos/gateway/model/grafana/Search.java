package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

public class Search {
	
	String target;
	String refId;
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	
	
	@Override
	public String toString() {
		return "Target: " + target + " RefId: " + refId;
	}
	

}
