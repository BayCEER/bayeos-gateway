package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Function extends UniqueEntity {
	@NotEmpty	 
    String name;

	public Function() {
	}
	
	public Function(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
    
}
