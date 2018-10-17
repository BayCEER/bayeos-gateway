package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardGroup;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardGroupRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;

@Controller
public class BoardGroupController extends AbstractController {

	@Autowired 
	BoardRepository repoBoard;
	
	@Autowired 
	BoardGroupRepository  repo;
			
	
	@RequestMapping(value="/groups/create",method=RequestMethod.GET)
	public String create(Model model){		
		BoardGroup g = new BoardGroup();
		g.setDomain(userSession.getUser().getDomain());		
		model.addAttribute("group",g);
		return "createBoardGroup";
	}
	
	@RequestMapping(value="/groups/save", method=RequestMethod.POST)
	public String save(@Valid BoardGroup group, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editBoardGroup";
		}				
		repo.save(userSession.getUser(),group);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/groups";
	}
	
	@RequestMapping(value="/groups",method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){		
		model.addAttribute("groups", repo.findAll(userSession.getUser(),domainFilter, pageable));
		return "listBoardGroup";
	}
	
	
	
	@RequestMapping(value="/groups/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){	
		model.addAttribute("group",repo.findOne(userSession.getUser(),id));
		return "editBoardGroup";		
	}
	
	@RequestMapping(value="/groups/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/groups";
	}
	
	@RequestMapping(value="/groups/selectBoards/{id}", method=RequestMethod.GET)
	public String selectBoards(@PathVariable Long id, Model model){
		
		BoardGroup g = repo.findOne(userSession.getUser(),id);
		model.addAttribute("group",g);
		
		List<Board> boards = repoBoard.findByBoardGroupIsNullAndDomain(g.getDomain());
		model.addAttribute("boards", boards);
		
		return "selectBoards";
	}
	
	@RequestMapping(value="/groups/addBoards", method=RequestMethod.POST)	
	public String addBoards(@RequestParam("id") Long id, @RequestParam("boards") List<Long> boards){			
		BoardGroup s = repo.findOne(userSession.getUser(),id);			
		for(Long i:boards){
				Board b = repoBoard.findOne(userSession.getUser(),i);
				b.setBoardGroup(s);
				repoBoard.save(userSession.getUser(),b);								
		}		
		return "redirect:/groups/" + s.getId();
	}
	
	@RequestMapping(value="/groups/removeBoard/{id}",method=RequestMethod.GET)
	public String removeBoard(@PathVariable("id") Long id){
		Board b = repoBoard.findOne(userSession.getUser(),id);
		Long g = b.getBoardGroup().getId();
		b.setBoardGroup(null);
		repoBoard.save(userSession.getUser(),b);
		return "redirect:/groups/" + g;
	}
	
	
	
	
	
	
	
	
	
	
}
