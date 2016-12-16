package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
public abstract class UniqueEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	@JsonView(DataTablesOutput.View.class)
	public Long id;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

}
