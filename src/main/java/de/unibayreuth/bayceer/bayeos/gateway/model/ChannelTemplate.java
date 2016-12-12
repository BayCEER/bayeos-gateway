package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
class ChannelTemplate extends CheckDevice {
	
	String nr;
	String label;
	String phenomena;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="unit_id", nullable = true)
	Unit unit;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="spline_id", nullable = true)
	Spline spline;	
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="aggr_interval_id", nullable = false)
	Interval aggrInterval;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="aggr_function_id", nullable = false)
	Function aggrFunction;			
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="board_template_id", nullable = false)
	BoardTemplate boardTemplate;

	public String getNr() {
		return nr;
	}

	public void setNr(String nr) {
		this.nr = nr;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPhenomena() {
		return phenomena;
	}

	public void setPhenomena(String phenomena) {
		this.phenomena = phenomena;
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
	
}
