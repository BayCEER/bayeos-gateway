package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
public class NamedDomainEntity extends DomainEntity {
	
	@JsonView(DataTablesOutput.View.class)		
	@Column(columnDefinition="text")
	protected String name;
		
	public NamedDomainEntity() {
		super();
	}
	
	public NamedDomainEntity(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

}
