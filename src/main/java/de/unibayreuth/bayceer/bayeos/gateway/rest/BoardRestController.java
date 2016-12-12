package de.unibayreuth.bayceer.bayeos.gateway.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;



@RestController
public class BoardRestController {
		
	@Autowired
	BoardRepository repo;
			
	// See https://datatables.net/manual/server-side
	// Module: https://github.com/darrachequesne/spring-data-jpa-datatables
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path ="/rest/boards", method = RequestMethod.POST)
	public DataTablesOutput<Board> findBoards(@Valid @RequestBody DataTablesInput input) {
		return repo.findAll(input);
	}
	
	
}