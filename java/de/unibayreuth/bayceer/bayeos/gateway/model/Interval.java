package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

import org.hibernate.annotations.Formula;

@Entity
public class Interval extends NamedDomainEntity {
	
	public Interval() {
		super();
	}
	
	public Interval(String name) {
		this.name = name;
	}
						
	@Formula("date_part('epoch',name::interval)")
	private Long epoch;
	

}
