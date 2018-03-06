package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;

public interface BoardRepository extends DataTablesRepository<Board, Long>{
	
	public List<Board> findByBoardGroupIsNull();

	public Board findByOrigin(String origin);	
}

