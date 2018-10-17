package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
public abstract class UniqueEntity implements Serializable  {
	
	public UniqueEntity() {
		super();
	}
		
	private static final long serialVersionUID = 2082710865576938551L;
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	@JsonView(DataTablesOutput.View.class)	
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	
		
		
	
}
