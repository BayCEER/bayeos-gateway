package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardGroup;
import de.unibayreuth.bayceer.bayeos.gateway.model.Board;

public interface BoardGroupRepository extends DomainEntityRepository<BoardGroup>{
		
     
}

