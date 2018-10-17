package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.Function;

public interface FunctionRepository extends DomainFilteredRepository<Function>{
			
	Function findFirstByName(String name);
	
}

