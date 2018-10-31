package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Formula;


@Entity
public class Channel extends UniqueEntity implements Comparable<Channel>{
	
	public Channel() {
		super();
	}



	private String nr;
	private String name;	
	private String phenomena;
	
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
		

	@Column(name="status_valid",insertable=false,updatable=false)	
	@Enumerated(EnumType.ORDINAL)
	private NagiosStatus statusValid;
		
	@Column(name="status_valid_msg",insertable=false,updatable=false)
	private String statusValidMsg;
		
	@Formula("(select get_channel_count(id))")
	private Integer channelCounts;		
	
	@Column(name="last_result_time",insertable=false,updatable=false)
	private Date lastResultTime;

	@Column(name="last_result_value",insertable=false,updatable=false)
	private Float lastResultValue;
	
	
	@Column(name="db_series_id")
	private Integer dbSeriesId;

	@Column(name="db_exclude_auto_export")
	private Boolean dbExcludeAutoExport = false;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="unit_id", nullable = true)
	private Unit unit;
	
	@ManyToOne()
	@JoinColumn(name="spline_id")
	Spline spline = new Spline();
	
	@ManyToOne()
	@JoinColumn(name="aggr_interval_id")
	Interval aggrInterval = new Interval();	
	
	@ManyToOne()
	@JoinColumn(name="aggr_function_id")
	Function aggrFunction = new Function();	
		
	
			
	
	private boolean isNr(String s){
		return s != null && s.matches("[0-9]+");
	}
	
		
	
	@Override
	public int compareTo(Channel o) {
		if (isNr(nr)  && isNr(o.nr)){
			return Integer.valueOf(nr).compareTo(Integer.valueOf(o.nr));
		} else {
			return nr.compareTo(o.nr);
		}	
	}
	
	public NagiosStatus getStatus(){	
		NagiosStatus com = getStatusComplete();
		NagiosStatus valid = getStatusValid();
	
		if (com == null || valid == null){
			return null;
		} else {			
			if (com.compareTo(valid) > 0){
				return com;
			} else {
				return valid;
			}			
		}
	}
	
	public String getQuantity(){		
		if (unit != null){
			return this.name + "[" + getUnit().getName() + "]";
		} else {
			return name;
		}
	}

	
	public NagiosStatus getStatusComplete() {
		if (channelCounts == null) {
			return null;
		}  else if (channelCounts > 8) {
			return NagiosStatus.ok;
		}  else if (channelCounts == 0) {
			return NagiosStatus.critical;
		}  else {
			return NagiosStatus.warn;
		}		   			
	}

	public Boolean getAutoExport(){
		return !this.dbExcludeAutoExport;
	}

	
	public Boolean getNagios() {
		return !excludeFromNagios;
	}
	
	public void setNagios(Boolean nagios) {
		this.excludeFromNagios = !nagios;
	}
	
	@Override
	public String toString() {
		return name;
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



	public Boolean getFilterCriticalValues() {
		return filterCriticalValues;
	}



	public void setFilterCriticalValues(Boolean filterCriticalValues) {
		this.filterCriticalValues = filterCriticalValues;
	}



	public NagiosStatus getStatusValid() {
		return statusValid;
	}



	public void setStatusValid(NagiosStatus statusValid) {
		this.statusValid = statusValid;
	}



	public String getStatusValidMsg() {
		return statusValidMsg;
	}



	public void setStatusValidMsg(String statusValidMsg) {
		this.statusValidMsg = statusValidMsg;
	}



	public Integer getChannelCounts() {
		return channelCounts;
	}



	public void setChannelCounts(Integer channelCounts) {
		this.channelCounts = channelCounts;
	}



	public Date getLastResultTime() {
		return lastResultTime;
	}



	public void setLastResultTime(Date lastResultTime) {
		this.lastResultTime = lastResultTime;
	}



	public Float getLastResultValue() {
		return lastResultValue;
	}



	public void setLastResultValue(Float lastResultValue) {
		this.lastResultValue = lastResultValue;
	}



	public Integer getDbSeriesId() {
		return dbSeriesId;
	}



	public void setDbSeriesId(Integer dbSeriesId) {
		this.dbSeriesId = dbSeriesId;
	}



	public Boolean getDbExcludeAutoExport() {
		return dbExcludeAutoExport;
	}



	public void setDbExcludeAutoExport(Boolean dbExcludeAutoExport) {
		this.dbExcludeAutoExport = dbExcludeAutoExport;
	}



	public Board getBoard() {
		return board;
	}



	public void setBoard(Board board) {
		this.board = board;
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
	


	
}
