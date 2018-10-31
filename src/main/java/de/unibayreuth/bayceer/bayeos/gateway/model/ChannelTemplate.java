package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ChannelTemplate extends UniqueEntity implements Comparable<ChannelTemplate>{
		
	private static final long serialVersionUID = -2463673550735591743L;
		

	String nr;
	String name;
	String phenomena;	
	
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
	
	
	@ManyToOne()
	@JoinColumn(name="unit_id")
	Unit unit;
	
	@ManyToOne()
	@JoinColumn(name="spline_id")
	Spline spline;
	
	@ManyToOne()
	@JoinColumn(name="aggr_interval_id")
	Interval aggrInterval;
	
	
	@ManyToOne()
	@JoinColumn(name="aggr_function_id")
	Function aggrFunction;			
		
	@ManyToOne()
	@JoinColumn(name="board_template_id", nullable = false)
	BoardTemplate boardTemplate;
	

	
	public ChannelTemplate(String nr, String name, String phenomena, 
			Unit unit, Spline spline, Interval aggrInterval, Function aggrFunction) {
		super();
		this.nr = nr;
		this.name = name;
		this.phenomena = phenomena;
		this.unit = unit;
		this.spline = spline;		
		this.aggrInterval = aggrInterval;
		this.aggrFunction = aggrFunction;
	}
		
		

	public ChannelTemplate() {
		super();
	}
	
	public ChannelTemplate(String name) {
		this.name = name;
	}



	private boolean isNr(String s){
		return s != null && s.matches("[0-9]+");
	}
	
	@Override
	public int compareTo(ChannelTemplate o) {		
		if (isNr(nr)  && isNr(o.nr)){
			return Integer.valueOf(nr).compareTo(Integer.valueOf(o.nr));
		} else {
			return nr.compareTo(o.nr);
		}		
	}
	
	@Override
	public String toString() {		
		StringBuffer f = new StringBuffer();
		f.append("Nr:").append(nr).append(",Name:").append(name).append(",Phenomena:").append(phenomena).append(",Unit:").append(unit).append(",Spline:").append(spline).append(",Interval:").append(aggrInterval);
		f.append(",Function:").append(aggrFunction);
		return f.toString();			
	}
			
	public Boolean getNagios() {
		return !excludeFromNagios;
	}
	
	public void setNagios(Boolean nagios) {
		this.excludeFromNagios = !nagios;
	}



	public String getNr() {
		return nr;
	}



	public void setNr(String nr) {
		this.nr = nr;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getPhenomena() {
		return phenomena;
	}



	public void setPhenomena(String phenomena) {
		this.phenomena = phenomena;
	}



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



	public Unit getUnit() {
		return unit;
	}



	public void setUnit(Unit unit) {
		this.unit = unit;
	}



	public Spline getSpline() {
		return spline;
	}



	public void setSpline(Spline spline) {
		this.spline = spline;
	}



	public Interval getAggrInterval() {
		return aggrInterval;
	}



	public void setAggrInterval(Interval aggrInterval) {
		this.aggrInterval = aggrInterval;
	}



	public Function getAggrFunction() {
		return aggrFunction;
	}



	public void setAggrFunction(Function aggrFunction) {
		this.aggrFunction = aggrFunction;
	}



	public BoardTemplate getBoardTemplate() {
		return boardTemplate;
	}



	public void setBoardTemplate(BoardTemplate boardTemplate) {
		this.boardTemplate = boardTemplate;
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
