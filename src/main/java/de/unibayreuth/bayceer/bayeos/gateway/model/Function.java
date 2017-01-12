package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Function extends UniqueEntity {
	
	
	public Function() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public Function(String id) {	
		if (id != null && !id.equals("")){			
			this.id = Long.valueOf(id);						
		}
	}



	@NotEmpty	 
    String name;

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
