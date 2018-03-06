package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Unit;

public interface UnitRepository extends PagingAndSortingRepository<Unit, Long>{
	
	Unit findFirstByName(String name);

}

