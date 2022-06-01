package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.NotificationRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.ContactRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UserRepository;

@Controller
public class ProfileController extends AbstractController {
	
	@Autowired
	UserRepository repo;
	
	@Autowired
	NotificationRepository repoNoti;
	
	@Autowired
	ContactRepository repoContact;
				
	@RequestMapping(value="/profile/save", method=RequestMethod.POST)
	public String save(@Valid User user, BindingResult bindingResult, RedirectAttributes redirect, Locale locale, Model model){			
		if (bindingResult.hasErrors()){			
			return "editProfile";
		}						
		
		User s = repo.findOne(userSession.getUser(), user.getId());		
		user.setPassword(s.getPassword());
		user.setDomain(s.getDomain());
				
		if (user.getContact().getEmail() == null) {						 
			user.setContact(null);									
		} else {				
			if (s.getContact() == null) {				
				Contact c = repoContact.findOneByEmailAndDomain(user.getContact().getEmail(),user.getDomain());
				if (c == null) {
					// Add New								
					Contact nc = new Contact();	
					nc.setDomain(user.getDomain());
					nc.setEmail(user.getContact().getEmail());				
					user.setContact(repoContact.save(nc));					
				} else {
					user.setContact(c);
				}
			} else {
				// Update contact 
				Contact c = s.getContact();
				c.setEmail(user.getContact().getEmail());				
				user.setContact(repoContact.save(c));
			}						
		}
		repo.save(userSession.getUser(),user);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/";
	}	
	
	
	@RequestMapping(value="/profile/edit", method=RequestMethod.GET)
	public String edit(Model model){	
		User u = repo.findOne(userSession.getUser(), userSession.getUser().getId());					
		model.addAttribute("user", u);		
		return "editProfile";		
	}
	
	
	
	
	
		
	

}
