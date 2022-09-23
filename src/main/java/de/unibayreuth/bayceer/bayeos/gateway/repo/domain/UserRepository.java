package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends DomainEntityRepository<User>{

	User findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalseAndPasswordIsNotNull(String string);

	User findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalseAndPasswordIsNotNull(String string,
			String string2);

	User findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalse(String string);

	User findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalse(String string, String string2);

		
		

}
