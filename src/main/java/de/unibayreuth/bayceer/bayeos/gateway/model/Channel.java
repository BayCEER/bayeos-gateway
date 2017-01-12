package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity 
public class Channel extends CheckDevice {

	private String nr;
	private String label;
	private String phenomena;

	@Column(name="last_result_time")
	private Date lastResultTime;

	@Column(name="last_result_value")
	private Float lastResultValue;

	
	@Column(name="spline_id")
	private Integer splineId;

	@Column(name="db_series_id")
	private Integer dbSeriesId;

	@Column(name="db_exclude_auto_export")
	private Boolean dbExcludeAutoExport = false;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="unit_id", nullable = true)
	private Unit unit;

	
	
	
	public Channel() {
	}

	public Channel(Board board, String nr) {
		this.nr = nr;
		this.board = board;
	}

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

	
}
