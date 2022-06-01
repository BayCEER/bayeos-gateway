package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;
import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.model.UserDTO;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.DomainRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UserRepository;

@RestController
public class UserRestController {
	
	@Autowired 
	UserRepository repo;
	
	@Autowired
	public UserSession userSession;
	
	@Autowired 
	DomainRepository repoDom;
	
	@RequestMapping(path = "/rest/users", method = RequestMethod.GET)
	public ResponseEntity findAll(@RequestParam(required = false) String domainName) {		
		List<User> users = null;
		if (domainName == null || domainName.isEmpty()) {			
			users = repo.findAll(userSession.getUser(),null);				
		} else {			
			Domain d = repoDom.findOneByName(domainName);			
			if (d != null) {
				DomainFilter f = new DomainFilter();
				f.setId(d.getId());			
				users = repo.findAll(userSession.getUser(), f);	
			} else {
				return new ResponseEntity<String>("Domain not found", HttpStatus.NOT_FOUND);
			}
		}		
		List<UserDTO> userDTOs = new ArrayList<UserDTO>(users.size()); 
		for(User u: users) {			
			userDTOs.add(new UserDTO(u));
		}
		return new ResponseEntity<List>(userDTOs,HttpStatus.OK);					
	}
	
	@RequestMapping(path = "/rest/users/{id}", method = RequestMethod.GET)
	public ResponseEntity findOneByID(@PathVariable Long id) {
		User u = repo.findOne(userSession.getUser(), id);
		if (u!=null) {
			u.setFullName();
			return new ResponseEntity<UserDTO>(new UserDTO(u), HttpStatus.OK);			
		} else {
			return new ResponseEntity<String>("User not found", HttpStatus.NOT_FOUND);
		}
	}
			
}
