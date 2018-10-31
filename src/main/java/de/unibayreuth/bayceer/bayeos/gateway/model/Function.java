package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

import de.unibayreuth.bayceer.bayeos.gateway.validator.FunctionExists;

@Entity
public class Function extends NamedDomainEntity {
	
		
	public Function(String name) {
		super(name);
	}
		
	public Function() {
		super();
	}

	@FunctionExists
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	public void setName(String name) {	
		super.setName(name);
	}
	    
}
