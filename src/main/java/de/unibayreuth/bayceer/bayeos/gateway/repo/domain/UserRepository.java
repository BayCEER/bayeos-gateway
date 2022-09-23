package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends DomainEntityRepository<User>{

		
	User findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalse(String name, String domainName);

	User findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalse(String name);
	

}
