package de.unibayreuth.bayceer.bayeos.gateway.repo;

import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;

public interface SplineRepository extends DomainFilteredRepository<Spline> {
		
	Spline findFirstByName(String name);
		
}
