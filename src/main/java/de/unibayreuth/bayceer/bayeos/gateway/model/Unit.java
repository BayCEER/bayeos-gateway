package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Unit extends NamedDomainEntity  {
	
	
	
	
	public Unit() {
		super();		
	}

	public Unit(String name) {
		super(name);
	}

	private String abbrevation;

	

	public String getAbbrevation() {
		return abbrevation;
	}

	public void setAbbrevation(String abbrevation) {
		this.abbrevation = abbrevation;
	}


	
}
