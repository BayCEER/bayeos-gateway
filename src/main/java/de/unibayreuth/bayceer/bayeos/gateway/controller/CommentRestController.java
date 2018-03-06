package de.unibayreuth.bayceer.bayeos.gateway.controller;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;
import de.unibayreuth.bayceer.bayeos.gateway.repo.CommentRepository;

@RestController
public class CommentRestController {
	@Autowired 
	CommentRepository repo;
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path ="/rest/comments/{id}", method = RequestMethod.POST)
	public DataTablesOutput<Comment> findComments(@Valid @RequestBody DataTablesInput input, @PathVariable final Long id) {		
		Specification<Comment> spec = new Specification<Comment>() {
			@Override
			public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("board").get("id"),id);
			}
		};		
		return repo.findAll(input, spec);
	}

	
	@RequestMapping(path="/rest/comments/delete/{id}", method= RequestMethod.GET)
	public void delete(@PathVariable Long id){
		repo.delete(id);		
	}
	
}
