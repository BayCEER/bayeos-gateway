package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;

public interface SplineRepository extends PagingAndSortingRepository<Spline, Long> {
		
	Spline findFirstByName(String name);
	
}
