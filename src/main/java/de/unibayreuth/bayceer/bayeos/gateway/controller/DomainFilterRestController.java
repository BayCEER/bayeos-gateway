package de.unibayreuth.bayceer.bayeos.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;

@RestController
public class DomainFilterRestController {
	
	@Autowired
	DomainFilter domainFilter;
	
	
	@PostMapping("/rest/domainFilter")	
	public void setDomainName(DomainFilter filter) {
		domainFilter.setId(filter.getId());
	}

}
