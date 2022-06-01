package de.unibayreuth.bayceer.bayeos.gateway.controller;
import javax.persistence.EntityNotFoundException;
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
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.MessageRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;

@RestController
public class MessageRestController extends AbstractController {
	
	@Autowired
	MessageRepository repo;
	
	@Autowired
	BoardRepository repoBoard;
	
	private Specification<Message> board(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("boardId"),id);
		};
	}
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/rest/messages/{id}", method = RequestMethod.POST)
	public DataTablesOutput<Message> findMessages(@Valid @RequestBody DataTablesInput input, @PathVariable Long id) {		
		Board b = repoBoard.findOne(userSession.getUser(),id);
		if (b== null) throw new EntityNotFoundException("Could not find board.");		
		return repo.findAll(input,null,board(b.getId()));
	}

}
