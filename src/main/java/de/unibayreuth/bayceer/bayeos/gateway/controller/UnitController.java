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
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UnitRepository;

@Controller
public class UnitController extends AbstractController{
	
	@Autowired
	UnitRepository repo;
	
	@RequestMapping(value="/units/create", method=RequestMethod.GET)
	public String create(Model model){
		Unit u = new Unit();
		u.setDomain(userSession.getUser().getDomain());		
		model.addAttribute("unit", u);	
		model.addAttribute("writeable",isWriteable(u));
		return "editUnit";
	}
		
	@RequestMapping(value="/units/save", method=RequestMethod.POST)
	public String save(@Valid Unit unit, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editUser";
		}				
		repo.save(userSession.getUser(),unit);		
		redirect.addFlashAttribute("globalMessage",getActionMsg("created", locale));
		return "redirect:/units";
	}
		
	@RequestMapping(value="/units", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){	
		model.addAttribute("units", repo.findAll(userSession.getUser(),domainFilter,pageable));
		return "listUnits";
	}
	
	@RequestMapping(value="/units/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		Unit u = repo.findOne(userSession.getUser(),id);
		model.addAttribute("unit",u);
		model.addAttribute("writeable",isWriteable(u));
		return "editUnit";		
	}
					
	@RequestMapping(value="/units/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;		
		return "redirect:/units";
	}

}
