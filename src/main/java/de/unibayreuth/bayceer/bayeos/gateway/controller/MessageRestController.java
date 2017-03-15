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

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.Message;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.MessageRepository;

@RestController
public class MessageRestController {
	@Autowired
	MessageRepository repo;

	@Autowired
	BoardRepository repoBoard;

	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/rest/messages/{id}", method = RequestMethod.POST)
	public DataTablesOutput<Message> findMessages(@Valid @RequestBody DataTablesInput input, @PathVariable Long id) {
		final Board b = repoBoard.findOne(id);
		Specification<Message> spec = new Specification<Message>() {
			@Override
			public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("origin"), b.getOrigin());
			}
		};
		return repo.findAll(input, spec);
	}

}
