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

import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.repo.SplineRepository;

@Controller
public class SplineController {
	
	@Autowired
	SplineRepository repo;
	
	@RequestMapping(value="/splines/create", method=RequestMethod.GET)
	public String create(Model model){
		model.addAttribute("spline", new Spline());		
		return "editSpline";
	}
	
	@RequestMapping(value="/splines/save", method=RequestMethod.POST)
	public String save(@Valid Spline spline, BindingResult bindingResult, RedirectAttributes redirect){
		if (bindingResult.hasErrors()){
			return "editSpline";
		}				
		repo.save(spline);
		redirect.addFlashAttribute("globalMessage", "Spline saved.");
		return "redirect:/splines";
	}
	
	
	@RequestMapping(value="/splines", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){
		model.addAttribute("splines", repo.findAll(pageable));
		return "listSpline";
	}
	
	@RequestMapping(value="/splines/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("spline",repo.findOne(id));
		return "editSpline";		
	}
	
	@RequestMapping(value="/splines/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage", "Spline removed");
		return "redirect:/splines";
	}
	
	
}
