package de.unibayreuth.bayceer.bayeos.gateway.model;

public class Observation {
	
	Long rowId;
	Long channelId;
	Long millis;
	Float value;
	
	
	public Long getRowId() {
		return rowId;
	}
	public void setRowId(Long rowId) {
		this.rowId = rowId;
	}
	public Long getChannelId() {
		return channelId;
	}
	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}
	public Long getMillis() {
		return millis;
	}
	public void setMillis(Long millis) {
		this.millis = millis;
	}
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	
	
	
	

}
