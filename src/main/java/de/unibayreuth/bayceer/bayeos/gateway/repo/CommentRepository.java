package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;

public interface CommentRepository extends PagingAndSortingRepository<Comment, Long>, JpaSpecificationExecutor {
	

	
}
