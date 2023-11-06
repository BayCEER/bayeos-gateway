package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.nio.file.AccessDeniedException;
import java.util.Locale;

import javax.persistence.EntityNotFoundException;
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

import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.DomainRepository;

@Controller
public class DomainController extends AbstractController {

	@Autowired
	DomainRepository repo;
	
	@RequestMapping(value="/domains/create", method=RequestMethod.GET)
	public String create(Model model) throws AccessDeniedException {
		if (userSession.getUser().inDefaultDomain()) {
			model.addAttribute("domain", new Domain());
			return "editDomain";
		} else {
			throw new AccessDeniedException("Failed to create domain");
		}
	}

	@RequestMapping(value = "/domains/save", method = RequestMethod.POST)
	public String save(@Valid Domain domain, BindingResult bindingResult, RedirectAttributes redirect, Locale locale) throws AccessDeniedException {
		if (!userSession.getUser().inDefaultDomain()) {
			throw new AccessDeniedException("Failed to save domain");
		}
				
		if (bindingResult.hasErrors()) {
			return "editDomain";
		}
		
		repo.save(domain);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/domains";
	}

	@RequestMapping(value = "/domains", method = RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable) throws AccessDeniedException {
		if (!userSession.getUser().inDefaultDomain()) {
			throw new AccessDeniedException("Failed to read domain");
		}
		model.addAttribute("domains", repo.findAll(pageable));		
		return "listDomains";
	}

	@RequestMapping(value = "/domains/{id}", method = RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model) throws AccessDeniedException {
		if (!userSession.getUser().inDefaultDomain()) {
			throw new AccessDeniedException("Failed to edit domain");
		}				
		model.addAttribute("domain", repo.findById(id).orElseThrow(()->new EntityNotFoundException()));
		return "editDomain";
	}
	
	@RequestMapping(value="/domain/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) throws AccessDeniedException {
		if (!userSession.getUser().inDefaultDomain()) {
			throw new AccessDeniedException("Failed to delete domain");
		}		
		repo.deleteById(id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/domains";
	}
	

}
