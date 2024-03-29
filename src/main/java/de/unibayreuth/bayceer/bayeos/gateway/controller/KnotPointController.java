package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Locale;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.KnotPointRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.SplineRepository;

@Controller
public class KnotPointController extends AbstractController{
	
	@Autowired
	KnotPointRepository repo;
	
	@Autowired 
	SplineRepository repoSpline;
	
	
	@RequestMapping(value="/knotpoints/create/{id}", method=RequestMethod.GET)
	public String create(@PathVariable Long id, Model model){
		Spline s = repoSpline.findOne(userSession.getUser(),id);
		if (s == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(s);	
		KnotPoint k = new KnotPoint();
		s.addKnotPoint(k);				
		model.addAttribute("knotPoint", k);		
		model.addAttribute("writeable",isWriteable(s));
		return "editKnotPoint";
	}
	
				
	@RequestMapping(value="/knotpoints/save", method=RequestMethod.POST)
	public String save(Model model, @Valid KnotPoint point, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			model.addAttribute("knotPoint", point);		
			model.addAttribute("writeable",isWriteable(point.getSpline()));
			return "editKnotPoint";
		}				
		checkWrite(repoSpline.findById(point.getSpline().getId()).orElseThrow(()-> new EntityNotFoundException()));
		repo.save(point);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));		
		return "redirect:/splines/" + point.getSpline().getId();
	}
		
	
	@RequestMapping(value="/knotpoints/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		KnotPoint k = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());				
		model.addAttribute("knotPoint",k);
		model.addAttribute("writeable",isWriteable(k.getSpline()));
		return "editKnotPoint";		
	}
	
		
	@RequestMapping(value="/knotpoints/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {		
		KnotPoint k = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());		
		checkWrite(k.getSpline());
		repo.deleteById(id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/splines/" + k.getSpline().getId();
	}
	
	

}
