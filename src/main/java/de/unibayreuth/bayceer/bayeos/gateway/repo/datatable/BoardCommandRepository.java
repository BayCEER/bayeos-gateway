package de.unibayreuth.bayceer.bayeos.gateway.repo.datatable;


import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardCommand;

public interface BoardCommandRepository extends DataTablesRepository<BoardCommand,Long> {
		
	@Query(nativeQuery = true, value = "select * from board_command bc join board b on (b.id = bc.board_id) where b.origin ilike ?1 and bc.response is null order by bc.insert_time limit 1")
	BoardCommand findFirstPendingByOrigin(String origin);
	
	@Query(nativeQuery = true, value = "select * from board_command bc join board b on (b.id = bc.board_id) where b.origin ilike ?1 and bc.response is null order by bc.insert_time asc")
	List<BoardCommand> findPendingByOrigin(String origin);
	
	
	@Query(nativeQuery = true, value = "select * from board_command bc where bc.board_id = ?1 order by bc.insert_time limit 1")
	List<BoardCommand> findAllByBoardId(Long id);
	
}
