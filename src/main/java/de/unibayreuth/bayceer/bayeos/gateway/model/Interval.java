package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Formula;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Interval extends UniqueEntity {
	
	@Column(name="name")
	@NotEmpty
    String name	;
	
	
	public Interval(String id) {
		if (id != null && !id.equals("")){			
			this.id = Long.valueOf(id);						
		}
	}
    
    public Interval() {
	
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Formula("date_part('epoch',name::interval)")
	private Long epoch;

	public Long getEpoch() {
		return epoch;
	}
	
	@Override
	public String toString() {
		return name;
	}
    	
}
