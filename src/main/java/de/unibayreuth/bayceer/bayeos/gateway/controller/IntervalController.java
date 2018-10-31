package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;
import de.unibayreuth.bayceer.bayeos.gateway.repo.IntervalRepository;

@Controller
public class IntervalController extends AbstractController {
	
	
	@Autowired
	IntervalRepository repo;
	
	@RequestMapping(value="/intervals/create", method=RequestMethod.GET)
	public String create(Model model){
		Interval i = new Interval();
		i.setDomain(userSession.getUser().getDomain());
		model.addAttribute("interval", i);		
		model.addAttribute("writeable",true);
		return "editInterval";
	}
	
	@RequestMapping(value="/intervals/save", method=RequestMethod.POST)
	public String save(Model model, @Valid Interval interval, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			model.addAttribute("interval", interval);		
			model.addAttribute("writeable",isWriteable(interval));
			return "editInterval";
		}				
		repo.save(userSession.getUser(),interval);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/intervals";
	}
	
	
	@RequestMapping(value="/intervals", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){
		// Map name sort order to epoch (derived property) 
		PageRequest r = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().getOrderFor("name").getDirection(), "epoch", "name");
		model.addAttribute("intervals", repo.findAll(userSession.getUser(),domainFilter,r));
		return "listInterval";
	}
	
	@RequestMapping(value="/intervals/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		Interval i = repo.findOne(userSession.getUser(),id);		
		model.addAttribute("interval",i);
		model.addAttribute("writeable",isWriteable(i));
		return "editInterval";		
	}
	
	@RequestMapping(value="/intervals/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/intervals";
	}
	

}
