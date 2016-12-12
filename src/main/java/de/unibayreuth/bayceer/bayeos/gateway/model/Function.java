package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

@Entity
class Function extends UniqueEntity {
	    
    String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
}
