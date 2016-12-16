package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;

public interface IntervalRepository extends PagingAndSortingRepository<Interval, Long>{
	
	

}
