package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;

public interface IntervalRepository extends DomainFilteredRepository<Interval>{
	
	Interval findFirstByName(String name);

		
}
