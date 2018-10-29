package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends DomainFilteredRepository<User>{

	User findFirstByNameAndDomainName(String name, String domainName);

	User findFirstByNameAndDomainIsNull(String name);
	
	

}
