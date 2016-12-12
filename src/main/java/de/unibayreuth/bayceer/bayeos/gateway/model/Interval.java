package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

@Entity
class Interval extends UniqueEntity {
	
    String name	;
	
	public Interval(String name) {
		this.name = name;
	}
    
    public Interval() {
	
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    	
}
