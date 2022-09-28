package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardCommand;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardCommandDTO;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.BoardCommandRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;



@RestController
public class BoardCommandRestController extends AbstractController {
	
	@Autowired
	BoardRepository repoBoard;
	
	@Autowired
	BoardCommandRepository repoCommand;
	
	
	// Create a new board command 
	@RequestMapping(path = "/rest/boardcommand", method = RequestMethod.POST)
	public ResponseEntity create (@RequestBody BoardCommandDTO cmd) {		
		Board board = repoBoard.findByOriginAndDomain(cmd.origin,userSession.getDomain());		
		if (board == null) {
			return new ResponseEntity<String>("Board not found", HttpStatus.BAD_REQUEST);
		}		
		
		User user = userSession.getUser();
		if (!user.inNullDomain()) {
			checkWrite(board);	
		}						
		BoardCommand c = repoCommand.save(new BoardCommand(user,new Date(),null,cmd.kind,cmd.value,cmd.description,null,null,board));		
		return new ResponseEntity<BoardCommandDTO>(
				new BoardCommandDTO(c),HttpStatus.CREATED);
			
	}
	
	// Find first pending board command by board origin
	@RequestMapping(path = "/rest/boardcommand", method = RequestMethod.GET)
	public ResponseEntity getPending (@RequestParam String origin) {
		Board board = repoBoard.findByOriginAndDomain(origin,userSession.getDomain());		
		if (board == null) {
			return new ResponseEntity<String>("Board not found", HttpStatus.BAD_REQUEST);
		}		
		User user = userSession.getUser();
		if (!user.inNullDomain()) {
			checkRead(board);	
		}				
		BoardCommand bc = repoCommand.findFirstPendingByOrigin(origin);		
		if (bc == null) {
			return new ResponseEntity<String>("No command found", HttpStatus.OK);
		}
		return new ResponseEntity<BoardCommandDTO>(new BoardCommandDTO(bc),HttpStatus.OK);				
	}
	
	// Find all board commands by board origin
	@RequestMapping(path = "/rest/boardcommands", method = RequestMethod.GET)
	public ResponseEntity getAll (@RequestParam String origin) {
		Board board = repoBoard.findByOriginAndDomain(origin,userSession.getDomain());		
		if (board == null) {
			return new ResponseEntity<String>("Board not found", HttpStatus.BAD_REQUEST);
		}		
		User user = userSession.getUser();
		if (!user.inNullDomain()) {
			checkRead(board);	
		}				
		List<BoardCommand> bcs = repoCommand.findAllByBoardId(board.getId());		
		List<BoardCommandDTO> r = new ArrayList<BoardCommandDTO>();
		for (BoardCommand b : bcs) {			
			r.add(new BoardCommandDTO(b));
		}		
		return new ResponseEntity<List<BoardCommandDTO>>(r,HttpStatus.OK);					
	}
	
	
	
	// Find board command by id 
	@RequestMapping(path = "/rest/boardcommand/{id}", method = RequestMethod.GET)
	public ResponseEntity get(@PathVariable Long id) {
		BoardCommand bc = repoCommand.findById(id).orElseThrow(()-> new EntityNotFoundException());		
		if (bc == null) {
			return new ResponseEntity<String>("No command found", HttpStatus.OK);
		}
		User user = userSession.getUser();
		if (!user.inNullDomain()) {
			checkRead(bc.getBoard());	
		}
		return new ResponseEntity<BoardCommandDTO>(new BoardCommandDTO(bc),HttpStatus.OK);
	}
	
	// Update board command response 
	@RequestMapping(path="/rest/boardcommand/{id}/response",method = RequestMethod.PATCH)
	public ResponseEntity setResponse(@PathVariable Long id, @RequestBody BoardCommandDTO cmd) {
		BoardCommand bc = repoCommand.findById(id).orElseThrow(()-> new EntityNotFoundException());		
		if (bc == null) {
			return new ResponseEntity<String>("No command found", HttpStatus.OK);
		}
		User user = userSession.getUser();
		if (!user.inNullDomain()) {
			checkWrite(bc.getBoard());	
		}
		bc.setResponse(cmd.response);
		bc.setResponseTime(new Date());
		bc.setResponseStatus(cmd.getResponseStatus());
		bc = repoCommand.save(bc);
		return new ResponseEntity<BoardCommandDTO>(new BoardCommandDTO(bc),HttpStatus.OK);
	}
	
	// Delete board command by id
	@RequestMapping(path="/rest/boardcommand/{id}", method = RequestMethod.DELETE)
	public ResponseEntity deleteCommandById(@PathVariable Long id) {
		
		BoardCommand bc = repoCommand.findById(id).orElseThrow(()-> new EntityNotFoundException());		
		if (bc == null) {
			return new ResponseEntity<String>("No command found", HttpStatus.OK);
		}
		User user = userSession.getUser();
		if (!user.inNullDomain()) {
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
		if (!user.inNullDomain()) {
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
