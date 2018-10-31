package de.unibayreuth.bayceer.bayeos.gateway.controller;

import static org.springframework.data.jpa.domain.Specifications.where;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;
import de.unibayreuth.bayceer.bayeos.gateway.repo.CommentRepository;


@RestController
public class CommentRestController extends AbstractController {
	
	
	@Autowired 
	CommentRepository repo;
	
	private Specification<Comment> boardId(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("board").get("id"),id);			
		};
	}
	
	private Specification<Comment> id(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("id"),id);			
		};
	}
	
	
	private Specification<Comment> domainId(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("board").get("domain").get("id"),id);
		};
	}
	
						
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path ="/rest/comments/{id}", method = RequestMethod.POST)
	public DataTablesOutput<Comment> findComments(@Valid @RequestBody DataTablesInput input, @PathVariable final Long id) {	
		if (userSession.getUser().inNullDomain()) {
				return repo.findAll(input, null,boardId(id) );			
		} else {
				return repo.findAll(input, null,where(boardId(id)).and(domainId(userSession.getUser().getDomainId())) );
		}
	}

	
	@RequestMapping(path="/rest/comments/delete/{id}", method= RequestMethod.GET)
	@Transactional
	public void delete(@PathVariable Long id){
		if (userSession.getUser().inNullDomain()) {
			repo.delete(id);	
		} else {
			if (repo.findOne(where(id(id)).and(domainId(userSession.getUser().getDomainId())))!=null) {
				repo.delete(id);
			} else {
				throw new AccessDeniedException("Failed to delete comment.");
			};
		}		
	}
	
}
