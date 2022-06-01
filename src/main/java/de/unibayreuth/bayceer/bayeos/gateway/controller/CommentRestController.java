package de.unibayreuth.bayceer.bayeos.gateway.controller;

import static org.springframework.data.jpa.domain.Specification.where;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;
import de.unibayreuth.bayceer.bayeos.gateway.model.CommentDTO;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.CommentRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UserRepository;


@RestController
public class CommentRestController extends AbstractController {

	@Autowired
	CommentRepository repo;

	@Autowired
	BoardRepository repoBoard;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	UserRepository repoUser;

	private Specification<Comment> boardId(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("board").get("id"), id);
		};
	}

	private Specification<Comment> id(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("id"), id);
		};
	}

	private Specification<Comment> domainId(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("board").get("domain").get("id"), id);
		};
	}
	
	private CommentDTOAdapter cAdapter = new CommentDTOAdapter();
	
	// Create 
	@RequestMapping(path = "/rest/comments", method = RequestMethod.POST)
	public ResponseEntity create(@RequestBody CommentDTO co) {		
		Comment cs = cAdapter.toEntity(co);
		checkWrite(repoBoard.findById(cs.getBoard().getId()).orElseThrow(()-> new EntityNotFoundException()));
		cs = repo.save(cs);				
		return new ResponseEntity<CommentDTO>(cAdapter.fromEntity(cs), HttpStatus.CREATED);
	}


	@RequestMapping(path="/rest/comments",method=RequestMethod.PUT)
	public ResponseEntity update(@RequestBody CommentDTO co) {		
		Comment cs = cAdapter.toEntity(co);	
		checkWrite(repoBoard.findById(cs.getBoard().getId()).orElseThrow(()-> new EntityNotFoundException()));
		cs = repo.save(cs);				
		return new ResponseEntity<CommentDTO>(cAdapter.fromEntity(cs), HttpStatus.OK);			
	}
	
	
	@RequestMapping(path = "/rest/comments/{id}", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity delete(@PathVariable Long id) {
		if (repo.existsById(id)) {
			if (userSession.getUser().inNullDomain()) {
				repo.deleteById(id);
			} else {
				if (repo.findOne(where(id(id)).and(domainId(userSession.getUser().getDomainId()))).orElseThrow(()-> new EntityNotFoundException()) != null) {
					repo.deleteById(id);
				} else {
					return new ResponseEntity<String>("Comment not found", HttpStatus.BAD_REQUEST);
				}
				;
			}
			return new ResponseEntity<String>("Comment deleted", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Comment not found", HttpStatus.BAD_REQUEST);
		}
	}
	
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/rest/comments/board/{id}", method = RequestMethod.POST)
	public DataTablesOutput<Comment> findComments(@Valid @RequestBody DataTablesInput input,
			@PathVariable final Long id) {
		if (userSession.getUser().inNullDomain()) {
			return repo.findAll(input, null, boardId(id));
		} else {
			return repo.findAll(input, null, where(boardId(id)).and(domainId(userSession.getUser().getDomainId())));
		}
	}

			
	@RequestMapping(path = "/rest/comments/board/{id}", method = RequestMethod.GET)
	public ResponseEntity list(@PathVariable Long id) {
		if (id == null) {
			return new ResponseEntity<String>("boardID missing", HttpStatus.BAD_REQUEST);
		}
		Board b = repoBoard.findOne(userSession.getUser(), id);
		if (b == null) {
			return new ResponseEntity<String>("Board not found", HttpStatus.BAD_REQUEST);
		}
		String sql = "select c.id as id, b.id as boardId, c.user_id as userID, c.insert_time as insertTime, c.\"content\" from board b \r\n"
				+ "join board_comment bc on b.id = bc.board_comments_id \r\n"
				+ "join \"comment\" c on c.id = bc.comment_id where b.id = ? order by c.id;";
		List<CommentDTO> coms = jdbcTemplate.query(sql, new Object[] { id },
				(rs, rowNum) -> new CommentDTO(rs.getLong(1), rs.getLong(2), rs.getLong(3),rs.getTimestamp(4).getTime(),rs.getString(5)));
		return new ResponseEntity<>(coms, HttpStatus.OK);

	}


	private class CommentDTOAdapter {		
		public CommentDTO fromEntity(Comment c) {
			Long boardID = (c.getBoard()!=null)?c.getBoard().getId():null;
			Long userID = (c.getUser()!=null)?c.getUser().getId():null;
			Long insertTime = (c.getInsertTime()!=null)?c.getInsertTime().getTime():new Date().getTime();					
			return new CommentDTO(c.getId(),boardID, userID, insertTime, c.getContent());				
		}
		
		public Comment toEntity(CommentDTO co) {
			Comment c = new Comment();
			
			c.setId(co.getId());
									
			// Board 
			if (co.getBoardID() != null) {
				c.setBoard(repoBoard.findOne(userSession.getUser(), co.getBoardID()));
			} else {				
				// ID			
				if (co.getId()!=null) {
					c.setBoard(repo.findById(co.getId()).orElseThrow(()-> new EntityNotFoundException()).getBoard());					
				} 				
			}
			// User
			if (co.getUserID() != null) {
				c.setUser(repoUser.findById(co.getUserID()).orElseThrow(()->new EntityNotFoundException()));				
			} else {
				c.setUser(userSession.getUser());
			}			
			// insertTime 
			if (co.getInsertTime() == null) {
				c.setInsertTime(new Date());				
			} else {
				c.setInsertTime(new Date(co.getInsertTime()));	
			}			
			// Content 
			c.setContent(co.getContent());			
			return c;									
		}		
	}
	
}


