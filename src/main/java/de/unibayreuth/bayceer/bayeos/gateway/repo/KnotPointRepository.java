package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint;

public interface KnotPointRepository extends PagingAndSortingRepository<KnotPoint, Long>{
	
	

}

