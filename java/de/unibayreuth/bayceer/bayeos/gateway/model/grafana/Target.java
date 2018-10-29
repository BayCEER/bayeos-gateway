package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Target {
	
	@JsonProperty("target")
	private String name;
	
	
	private String refId;

	public Target() {
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}
}