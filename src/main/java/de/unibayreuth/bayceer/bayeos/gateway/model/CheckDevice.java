package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.Column;

@MappedSuperclass
public abstract class CheckDevice extends UniqueEntity {	
	// Validation Check
	@Column(name="critical_max")
	Float criticalMax;
	@Column(name="critical_min")
	Float criticalMin; 
	@Column(name="warning_max")
	Float warningMax;
	@Column(name="warning_min")
	Float warningMin;	
	// Completeness Check
	@Column(name="sampling_interval")
	Integer samplingInterval;	
	// Check Delay
	@Column(name="check_delay")
	Integer checkDelay;
	// Disable Alerts   
	@Column(name="exclude_from_nagios")
	Boolean excludeFromNagios = false;	
	// Import valid records only
	@Column(name="filter_critical_values")
	Boolean filterCriticalValues = false;
	
	public Float getCriticalMax() {
		return criticalMax;
	}
	public void setCriticalMax(Float criticalMax) {
		this.criticalMax = criticalMax;
	}
	public Float getCriticalMin() {
		return criticalMin;
	}
	public void setCriticalMin(Float criticalMin) {
		this.criticalMin = criticalMin;
	}
	public Float getWarningMax() {
		return warningMax;
	}
	public void setWarningMax(Float warningMax) {
		this.warningMax = warningMax;
	}
	public Float getWarningMin() {
		return warningMin;
	}
	public void setWarningMin(Float warningMin) {
		this.warningMin = warningMin;
	}
	public Integer getSamplingInterval() {
		return samplingInterval;
	}
	public void setSamplingInterval(Integer samplingInterval) {
		this.samplingInterval = samplingInterval;
	}
	public Integer getCheckDelay() {
		return checkDelay;
	}
	public void setCheckDelay(Integer checkDelay) {
		this.checkDelay = checkDelay;
	}
	public Boolean getExcludeFromNagios() {
		return excludeFromNagios;
	}
	public void setExcludeFromNagios(Boolean excludeFromNagios) {
		this.excludeFromNagios = excludeFromNagios;
	}
	public Boolean getFilterCriticalValues() {
		return filterCriticalValues;
	}
	public void setFilterCriticalValues(Boolean filterCriticalValues) {
		this.filterCriticalValues = filterCriticalValues;
	}
	
	
			
   
	
	
}
