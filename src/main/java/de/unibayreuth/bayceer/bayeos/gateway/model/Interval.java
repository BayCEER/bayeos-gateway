package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;

import org.hibernate.annotations.Formula;

import de.unibayreuth.bayceer.bayeos.gateway.validator.TimeInterval;

@Entity
public class Interval extends NamedDomainEntity {
	
	

	@Formula("date_part('epoch',name::interval)")
	private Long epoch;

	
	public Interval() {
		super();
	}
	
	public Interval(String string) {
		super(string);
	}
	
	
	@TimeInterval
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	public void setName(String name) {	
		super.setName(name);
	}
}
