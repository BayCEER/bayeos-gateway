package de.unibayreuth.bayceer.bayeos.gateway.controller;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardCommand;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.BoardCommandRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;



@RestController
public class BoardCommandRestController extends AbstractController {
	
	@Autowired
	BoardRepository repoBoard;
	
	@Autowired
	BoardCommandRepository repoCommand;
	
		
	
	// Delete board command by id
	@RequestMapping(path="/rest/boardcommand/{id}", method = RequestMethod.DELETE)
	public ResponseEntity deleteCommandById(@PathVariable Long id) {
		
		BoardCommand bc = repoCommand.findById(id).orElseThrow(()-> new EntityNotFoundException());		
		if (bc == null) {
			return new ResponseEntity<String>("No command found", HttpStatus.OK);
		}
		User user = userSession.getUser();
		if (!user.inDefaultDomain()) {
			checkWrite(bc.getBoard());	
		}		
		repoCommand.delete(bc);
		return new ResponseEntity<String>("Command deleted", HttpStatus.OK);
	}
	
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/rest/boardcommands/{id}", method = RequestMethod.POST)
	public DataTablesOutput<BoardCommand> findComments(@Valid @RequestBody DataTablesInput input,
			@PathVariable final Long id) {
		
		Board board = repoBoard.findById(id).orElseThrow(()-> new EntityNotFoundException());	;		
		User user = userSession.getUser();
		if (!user.inDefaultDomain()) {
			checkRead(board);	
		}									
		return repoCommand.findAll(input, null, boardId(id));
					
	}
	
	private Specification<BoardCommand> boardId(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("board").get("id"), id);
		};
	}

}
