package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;

public interface SplineRepository extends DomainFilteredRepository<Spline> {
		
	Spline findFirstByName(String name);
	
	@Query(nativeQuery=true, value="select s.* from spline s order by s.name")
	List<Spline> findAllSplines();
	
	@Query(nativeQuery=true, value="select s.* from spline s where (s.domain_id is null or s.domain_id = :domainID) order by s.name")
	List<Spline> findSplines(Long domainID);
	
	
}
