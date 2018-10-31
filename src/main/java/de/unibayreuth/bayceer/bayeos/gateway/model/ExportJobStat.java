package de.unibayreuth.bayceer.bayeos.gateway.model;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ExportJobStat extends UniqueEntity {	
	
	@Column(name="start_time")
	Date startTime;
	
	@Column(name="end_time")
	Date endTime;
	
	Integer exported;		
	Integer status;
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Integer getExported() {
		return exported;
	}
	public void setExported(Integer exported) {
		this.exported = exported;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
}
