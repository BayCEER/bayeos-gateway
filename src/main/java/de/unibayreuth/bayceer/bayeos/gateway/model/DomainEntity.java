package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
public abstract class DomainEntity extends UniqueEntity {
	
	public DomainEntity() {
		super();
	}
	
	
	@ManyToOne	
	@JoinColumn(name = "domain_id", nullable=true)	
	@JsonView(DataTablesOutput.View.class)
	protected Domain domain;
		
	public Long getDomainId() {
		if (domain != null) {
			return domain.getId();
		} else {
			return null;
		}
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	
	
	
			
}
