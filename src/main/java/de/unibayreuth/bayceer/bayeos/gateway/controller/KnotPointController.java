package de.unibayreuth.bayceer.bayeos.gateway.controller;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.repo.KnotPointRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.SplineRepository;

@Controller
public class KnotPointController {
	
	@Autowired
	KnotPointRepository repo;
	
	@Autowired 
	SplineRepository repoSpline;
	
	
	@RequestMapping(value="/knotpoints/create/{id}", method=RequestMethod.GET)
	public String create(@PathVariable Long id, Model model){
		Spline s = repoSpline.findOne(id);						
		KnotPoint k = new KnotPoint();
		k.setSpline(s);
		model.addAttribute("knotPoint", k);		
		return "editKnotPoint";
	}
	
				
	@RequestMapping(value="/knotpoints/save", method=RequestMethod.POST)
	public String save(@Valid KnotPoint point, BindingResult bindingResult, RedirectAttributes redirect){
		if (bindingResult.hasErrors()){
			return "editKnotPoint";
		}				
		repo.save(point);
		redirect.addFlashAttribute("globalMessage", "Point saved.");		
		return "redirect:/splines/" + point.getSpline().id;
	}
		
	
	@RequestMapping(value="/knotpoints/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("knotPoint",repo.findOne(id));
		return "editKnotPoint";		
	}
	
		
	@RequestMapping(value="/knotpoints/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect) {		
		KnotPoint k = repo.findOne(id);	
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage", "Knot point removed successfully");
		return "redirect:/splines/" + k.getSpline().id;
	}
	
	

}
