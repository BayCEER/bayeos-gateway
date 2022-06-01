package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;

public interface IntervalRepository extends DomainEntityRepository<Interval>{
	
	Interval findFirstByName(String name);

		
}
