package de.unibayreuth.bayceer.bayeos.gateway.repo.datatable;


import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;

public interface CommentRepository extends DataTablesRepository<Comment, Long> {
		
	
}
