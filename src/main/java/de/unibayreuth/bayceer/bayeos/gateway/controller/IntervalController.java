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
public class IntervalController extends AbstractCRUDController {
	
	
	@Autowired
	IntervalRepository repo;
	
	@RequestMapping(value="/intervals/create", method=RequestMethod.GET)
	public String create(Model model){
		model.addAttribute("interval", new Interval());		
		return "editInterval";
	}
	
	@RequestMapping(value="/intervals/save", method=RequestMethod.POST)
	public String save(@Valid Interval interval, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editInterval";
		}				
		repo.save(interval);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/intervals";
	}
	
	
	@RequestMapping(value="/intervals", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){
		// Map name sort order to epoch (derived property) 
		PageRequest r = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().getOrderFor("name").getDirection(), "epoch", "name");
		model.addAttribute("intervals", repo.findAll(r));
		return "listInterval";
	}
	
	@RequestMapping(value="/intervals/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("interval",repo.findOne(id));
		return "editInterval";		
	}
	
	@RequestMapping(value="/intervals/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/intervals";
	}
	

}
