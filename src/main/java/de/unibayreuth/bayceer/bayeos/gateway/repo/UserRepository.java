package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends DomainFilteredRepository<User>{

			

	User findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalseAndPasswordIsNotNull(String string);

	User findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalseAndPasswordIsNotNull(String string, String string2);

	User findFirstByNameIgnoreCaseAndDomainIsNullAndLockedIsFalse(String string);

	User findFirstByNameIgnoreCaseAndDomainNameIgnoreCaseAndLockedIsFalse(String string, String string2);

}
