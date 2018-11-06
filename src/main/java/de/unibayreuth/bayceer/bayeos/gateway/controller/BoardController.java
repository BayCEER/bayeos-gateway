package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Collections;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardGroupRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.service.BoardTemplateService;



@Controller
public class BoardController extends AbstractController {
				
	@Autowired
	DataTableSearch boardSearch;
	
	@Autowired
	BoardTemplateRepository repoBoardTemplate;
	
	@Autowired 
	BoardGroupRepository repoGroup;
	
	@Autowired
	BoardTemplateService serviceBoardTemplate;
	
	@Autowired
	BoardRepository repo;
		
		
	@RequestMapping(value={"/","/boards"}, method=RequestMethod.GET)	
	public String list(Model model) {
		model.addAttribute("boardSearch",boardSearch);		
		return "listBoard";
		
	}
	
	
	@RequestMapping(path="/boards/{id}", method = RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model, @RequestParam(defaultValue="channels") String tab){			
		Board b = repo.findOne(userSession.getUser(),id);						
		if (b!=null && b.getChannels()!=null){
			Collections.sort(b.getChannels());
		}
		model.addAttribute("board", b);
		model.addAttribute("groups", repoGroup.findAll(userSession.getUser(),null));
		model.addAttribute("tab",tab);		
		return "editBoard";
	}
	
	
	@RequestMapping(path="/boards/delete/{id}", method=RequestMethod.GET)
    public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);		
        redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
        return "redirect:/boards";
    }
	
	
	
	
	@RequestMapping(value="/boards", params={"save"}, method=RequestMethod.POST)
	public String save(@Valid Board board, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editBoard";
		}			
		repo.save(userSession.getUser(), board);
		redirect.addFlashAttribute("globalMessage",getActionMsg("saved", locale));
		return "redirect:/boards";		
	}
	
	
	
	@RequestMapping(value="/boards/applyTemplate", method=RequestMethod.POST)	
	public String applyTemplate(@RequestParam("templateId") Long templateId, @RequestParam("boardId") Long boardId, RedirectAttributes redirect, Locale locale){		
		serviceBoardTemplate.applyTemplate(boardId, templateId);		
		redirect.addFlashAttribute("globalMessage", msg.getMessage("e.template", null, locale) + " " +  msg.getMessage("action.applied", null, locale));
		return "redirect:/boards/" + boardId;	
	}
	
	
	
	@RequestMapping(value="/boards/selectTemplate/{id}", method=RequestMethod.GET)
	public String selectBoardTemplate(@PathVariable Long id, Model model){		
		model.addAttribute("boardTemplates", repoBoardTemplate.findAllSortedByName(userSession.getUser(),null));
		model.addAttribute("boardId", id);		
		return "selectBoardTemplate";		
	}
	
	
	
	@RequestMapping(value="/boards/saveAsTemplate/{id}", method=RequestMethod.GET)
	public String saveAsTemplate(@PathVariable Long id){		
		BoardTemplate t = serviceBoardTemplate.saveAsTemplate(id);
		return "redirect:/boardTemplates/" + t.getId();
	}
	
	
	@RequestMapping(value="/boards/chart/{id}")
	public String chartBoard(@PathVariable Long id, Model model){
		Board b = repo.findOne(userSession.getUser(),id);		
		if (b!=null && b.getChannels()!=null){
			Collections.sort(b.getChannels());
		}
		model.addAttribute("board",b);
		return "chartBoard";
	}
	
	
	
	
	
	
	
	
	
	
	

	
	
	

	
}