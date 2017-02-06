package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using=DataPointSerializer.class)
public class DataPoint {
	
	Float value;
	Long millis;
		
	public DataPoint(Float value, Long millis) {
		super();
		this.value = value;
		this.millis = millis;
	}
	
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public Long getMillis() {
		return millis;
	}
	public void setMillis(Long millis) {
		this.millis = millis;
	}	

	
	
}
