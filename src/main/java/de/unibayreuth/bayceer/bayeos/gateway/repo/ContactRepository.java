package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;

public interface ContactRepository extends DomainFilteredRepository<Contact> {
	
	public Contact findOneByEmailAndDomain(String email,Domain domain);	
	public Contact findOneByEmailAndDomainIsNull(String email);


}
