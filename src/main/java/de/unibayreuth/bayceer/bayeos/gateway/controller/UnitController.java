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

import de.unibayreuth.bayceer.bayeos.gateway.model.Unit;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UnitRepository;

@Controller
public class UnitController extends AbstractCRUDController{
	
	@Autowired
	UnitRepository repo;
	
	@RequestMapping(value="/units/create", method=RequestMethod.GET)
	public String create(Model model){
		model.addAttribute("unit", new Unit());		
		return "editUnit";
	}
		
	@RequestMapping(value="/units/save", method=RequestMethod.POST)
	public String save(@Valid Unit unit, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editUser";
		}				
		repo.save(unit);
		
		redirect.addFlashAttribute("globalMessage",getActionMsg("created", locale));
		return "redirect:/units";
	}
		
	@RequestMapping(value="/units", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){	
		model.addAttribute("units", repo.findAll(pageable));
		return "listUnits";
	}
	
	@RequestMapping(value="/units/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("unit",repo.findOne(id));
		return "editUnit";		
	}
					
	@RequestMapping(value="/units/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;		
		return "redirect:/units";
	}

}
