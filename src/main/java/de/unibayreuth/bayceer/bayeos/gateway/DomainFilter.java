package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.DomainRepository;



@SessionScope
@Component
public class DomainFilter {
	
	@Autowired
	private DomainRepository repo;
		
	private Long id;
	
	public Iterable<Domain> getDomains(){
		return repo.findAll();
	}

	public DomainRepository getRepo() {
		return repo;
	}

	public void setRepo(DomainRepository repo) {
		this.repo = repo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
		

	
	
	
}
