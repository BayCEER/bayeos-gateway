package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Domain extends UniqueEntity {
	
	public Domain() {
		super();
	}
	
	public Domain(String name) {
		this.name = name;
	}
				
	@Column(unique=true,nullable=false)
	@JsonView(DataTablesOutput.View.class)
    String name;
	
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
