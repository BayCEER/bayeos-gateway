package de.unibayreuth.bayceer.bayeos.gateway.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.repo.FunctionRepository;

@Controller
public class FunctionController {
	
	
	@Autowired
	FunctionRepository repo;
	
	@RequestMapping(value="/functions/create", method=RequestMethod.GET)
	public String create(Model model){
		model.addAttribute("function", new Function());		
		return "editFunction";
	}
	
	@RequestMapping(value="/functions/save", method=RequestMethod.POST)
	public String save(@Valid Function function, BindingResult bindingResult, RedirectAttributes redirect){
		if (bindingResult.hasErrors()){
			return "editFunction";
		}				
		repo.save(function);
		redirect.addFlashAttribute("globalMessage", "Function saved.");
		return "redirect:/functions";
	}
	
	
	@RequestMapping(value="/functions", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){	
		model.addAttribute("functions", repo.findAll(pageable));
		return "listFunction";
	}
	
	@RequestMapping(value="/functions/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("function",repo.findOne(id));
		return "editFunction";		
	}
	
	@RequestMapping(value="/functions/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage", "Function removed successfully");
		return "redirect:/functions";
	}
	

}
