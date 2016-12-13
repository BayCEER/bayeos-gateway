package de.unibayreuth.bayceer.bayeos.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;



@Controller
public class BoardController {
		
	@Autowired
	BoardRepository repo;
	
	@RequestMapping(value={"/","/boards"}, method=RequestMethod.GET)	
	public String list() {
		return "listBoard";
		
	}
	
	@RequestMapping(path="/boards/{id}", method = RequestMethod.GET)
	public String editBoard(@PathVariable Long id, Model model){	
		Board b = repo.findOne(id);
		model.addAttribute("board", b);					
		return "editBoard";
	}
	
	
	@RequestMapping(path="/boards/{id}", method=RequestMethod.DELETE)
    public String delete(@PathVariable Long id , RedirectAttributes redirect) {
        repo.delete(id);
        redirect.addFlashAttribute("globalMessage", "Board removed successfully");
        return "redirect:/boards";
    }
		

}