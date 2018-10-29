package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.Unit;

public interface UnitRepository extends DomainFilteredRepository<Unit>{
	Unit findFirstByName(String name);
		

}

