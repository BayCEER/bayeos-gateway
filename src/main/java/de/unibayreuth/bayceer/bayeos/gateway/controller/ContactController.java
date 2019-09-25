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

import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.repo.ContactRepository;

@Controller
public class ContactController extends AbstractController {
	

	@Autowired
	ContactRepository repo;
	
	@RequestMapping(value="/contacts/create", method=RequestMethod.GET)
	public String create(Model model){
		Contact c = new Contact();		
		c.setDomain(userSession.getUser().getDomain());
		model.addAttribute("contact", c);				
		return "editContact";
	}
	
	
	@RequestMapping(value="/contacts/save", method=RequestMethod.POST)
	public String save(Model model, @Valid Contact contact, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			model.addAttribute("contact", contact);					
			return "editContact";
		}				
		repo.save(userSession.getUser(),contact);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/contacts";
	}
	
	@RequestMapping(value="/contacts", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("email") Pageable pageable){		
		model.addAttribute("contacts", repo.findAll(userSession.getUser(),domainFilter,pageable));
		return "listContact";
	}
	
	@RequestMapping(value="/contacts/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		Contact c = repo.findOne(userSession.getUser(),id);		
		model.addAttribute("contact",c);		
		return "editContact";		
	}
	
	@RequestMapping(value="/contacts/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/contacts";
	}

}
