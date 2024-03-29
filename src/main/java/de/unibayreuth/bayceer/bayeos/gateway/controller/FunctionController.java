package de.unibayreuth.bayceer.bayeos.gateway.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.FunctionRepository;

@Controller
public class FunctionController extends AbstractController{
	
	
	@Autowired
	FunctionRepository repo;
	
				
	@RequestMapping(value="/functions/create", method=RequestMethod.GET)
	public String create(Model model){
		Function f = new Function();
		f.setDomain(userSession.getUser().getDomain());		
		model.addAttribute("function", f);	
		model.addAttribute("writeable",isWriteable(f));
		return "editFunction";
	}
	
	@RequestMapping(value="/functions/save", method=RequestMethod.POST)
	public String save(Model model, @Valid Function function, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){								
		if (bindingResult.hasErrors()){		
			model.addAttribute("function",function);
			model.addAttribute("writeable",isWriteable(function));
			return "editFunction";
		}	
		repo.save(userSession.getUser(),function);		
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/functions";
	}
	
	
	@RequestMapping(value="/functions", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){	
		model.addAttribute("functions", repo.findAll(userSession.getUser(),domainFilter, pageable));
		return "listFunction";
	}
	
	@RequestMapping(value="/functions/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		Function f = repo.findOne(userSession.getUser(),id);
		model.addAttribute("function",f);
		model.addAttribute("writeable",isWriteable(f));
		return "editFunction";		
	}
	
	@RequestMapping(value="/functions/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/functions";
	}
	

}
