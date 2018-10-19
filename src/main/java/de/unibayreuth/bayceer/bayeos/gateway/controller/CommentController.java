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
import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.CommentRepository;

@Controller
public class CommentController extends AbstractController {
	
	@Autowired
	CommentRepository repo;
	
	@Autowired 
	BoardRepository repoBoard;
				
	
	@RequestMapping(value="/comments/create/{id}", method=RequestMethod.GET)
	public String create(@PathVariable Long id, Model model){		
		model.addAttribute("comment", new Comment());
		model.addAttribute("board", id);
		return "editComment";
	}
	
	@RequestMapping(value="/comments/edit/{id}")
	public String edit(@PathVariable Long id, Model model){
		Comment c = repo.findOne(id);
		if (c == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(repoBoard.findOne(c.getBoard().getId()));		
		model.addAttribute("comment",c);
		model.addAttribute("board",c.getBoard().getId());
		return "editComment";
		
	}
	
	@RequestMapping(value="/comments/save", method=RequestMethod.POST)
	@Transactional
	public String save(@Valid Comment com, @ModelAttribute("board") Long id, RedirectAttributes redirect, Locale locale){
		Board b = repoBoard.findOne(id);
		if (b == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(b);
		com.setBoard(b);		
		com.setUser(userSession.getUser());		
		repo.save(com);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));		
		return "redirect:/boards/" + id + "?tab=comments";
	}
	
	
		
}
