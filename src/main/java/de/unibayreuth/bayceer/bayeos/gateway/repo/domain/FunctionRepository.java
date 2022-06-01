package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import de.unibayreuth.bayceer.bayeos.gateway.model.Function;

public interface FunctionRepository extends DomainEntityRepository<Function>{
			
	Function findFirstByName(String name);
	
}

