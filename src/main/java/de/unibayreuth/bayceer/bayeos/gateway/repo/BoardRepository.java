package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;

@Repository
public interface BoardRepository extends DomainFilteredRepository<Board>{
	
	public List<Board> findByBoardGroupIsNullAndDomain(Domain domain);

	public Board findByOriginAndDomain(String origin, Domain domain);
			
		
}

