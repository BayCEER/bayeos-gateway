package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Unit extends UniqueEntity  {
	
	@Column(nullable=false)
	private String name;

	private String abbrevation;

	@Column(name="db_unit_id")
	private Integer dbUnitId;
	
	

	public Unit() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Unit(String id){
		if (id != null && !id.equals("")){			
			this.id = Long.valueOf(id);						
		}
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbrevation() {
		return abbrevation;
	}

	public void setAbbrevation(String abbrevation) {
		this.abbrevation = abbrevation;
	}

	public Integer getDbUnitId() {
		return dbUnitId;
	}

	public void setDbUnitId(Integer dbUnitId) {
		this.dbUnitId = dbUnitId;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	
}
