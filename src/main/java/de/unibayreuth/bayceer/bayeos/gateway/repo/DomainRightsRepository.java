package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.security.access.AccessDeniedException;

import de.unibayreuth.bayceer.bayeos.gateway.model.DomainEntity;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface DomainRightsRepository<T extends DomainEntity> extends DataTablesRepository<T, Long> {
	
	default void checkWrite(User u, DomainEntity d) {		
		if (u.getDomain()!=null && u.getDomain()  != d. getDomain()) {
			throw new AccessDeniedException("Missing rights to save domain object"); 
		}
	}
	
	default void checkRead(User u, DomainEntity d) {
		if (u.getDomain() != null && u.getDomain()!= d.getDomain() && !d.getDomain().getName().matches(DomainFilteredRepository.nullDomainReadAble)){
			throw new AccessDeniedException("Missing rights to read domain object");
		}
		
	}

}
