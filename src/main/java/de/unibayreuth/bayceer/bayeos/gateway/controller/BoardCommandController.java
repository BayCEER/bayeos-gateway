package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Locale;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardCommand;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.BoardCommandRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;

@Controller
public class BoardCommandController extends AbstractController {
	
	@Autowired
	BoardCommandRepository repo;
	
	@Autowired 
	BoardRepository repoBoard;
				
	
	@RequestMapping(value="/boardCommands/create/{id}", method=RequestMethod.GET)
	public String create(@PathVariable Long id, Model model){		
		model.addAttribute("command", new BoardCommand());
		model.addAttribute("board", id);
		return "editBoardCommand";
	}
	
	@RequestMapping(value="/boardCommands/edit/{id}")
	public String edit(@PathVariable Long id, Model model){
		BoardCommand c = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());
		if (c == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(repoBoard.findById(c.getBoard().getId()).orElseThrow(() -> new EntityNotFoundException()));		
		model.addAttribute("command",c);
		model.addAttribute("board",c.getBoard().getId());
		return "editBoardCommand";
		
	}
	
	@RequestMapping(value="/boardCommands/save", method=RequestMethod.POST)
	@Transactional
	public String save(@Valid BoardCommand cmd, @ModelAttribute("board") Long id, RedirectAttributes redirect, Locale locale){
		Board b = repoBoard.findById(id).orElseThrow(()-> new EntityNotFoundException());
		if (b == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(b);
		cmd.setBoard(b);		
		cmd.setUser(userSession.getUser());		
		repo.save(cmd);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));		
		return "redirect:/boards/" + id + "?tab=commands";
	}
	
	
		
}
