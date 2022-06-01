package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;

public interface ContactRepository extends DomainEntityRepository<Contact> {
	
	public Contact findOneByEmailAndDomain(String email,Domain domain);	
	public Contact findOneByEmailAndDomainIsNull(String email);


}
