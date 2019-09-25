package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

@Entity
public class Contact extends DomainEntity {
	String email;
		
		
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}	
	
	
	
}
