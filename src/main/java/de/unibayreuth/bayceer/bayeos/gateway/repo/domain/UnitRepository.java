package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import de.unibayreuth.bayceer.bayeos.gateway.model.Unit;

public interface UnitRepository extends DomainEntityRepository<Unit>{
	Unit findFirstByName(String name);
		

}

