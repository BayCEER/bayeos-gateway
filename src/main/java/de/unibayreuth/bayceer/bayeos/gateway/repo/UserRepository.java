package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends DomainFilteredRepository<User>{

			

	User findFirstByNameAndDomainIsNullAndLockedIsFalseAndPasswordIsNotNull(String string);

	User findFirstByNameAndDomainNameAndLockedIsFalseAndPasswordIsNotNull(String string, String string2);

	User findFirstByNameAndDomainIsNullAndLockedIsFalse(String string);

	User findFirstByNameAndDomainNameAndLockedIsFalse(String string, String string2);

}
