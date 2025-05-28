package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardGroup;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;

@Repository
public interface BoardRepository extends DomainEntityRepository<Board>{
	
	public List<Board> findByBoardGroupIsNullAndDomain(Domain domain);
	
	public List<Board> findByBoardGroup(BoardGroup g);
	
	public Board findByOriginAndDomain(String origin, Domain domain);
	
	public Board findByIdAndDomain(Long id, Domain domain);
			
		
}

