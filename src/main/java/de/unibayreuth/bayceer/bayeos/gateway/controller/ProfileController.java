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

import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;

@Controller
public class ProfileController extends AbstractController {
	
	@Autowired
	UserRepository repo;
				
	@RequestMapping(value="/profile/save", method=RequestMethod.POST)
	public String save(@Valid User user, BindingResult bindingResult, RedirectAttributes redirect, Locale locale, Model model){			
		if (bindingResult.hasErrors()){			
			return "editProfile";
		}						
		User u = repo.findOne(userSession.getUser().getId());
		u.setFirstName(user.getFirstName());
		u.setLastName(user.getLastName());				
		repo.save(u);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/";
	}		
	
	@RequestMapping(value="/profile/edit", method=RequestMethod.GET)
	public String edit(Model model){	
		User u = repo.findOne(userSession.getUser().getId());
		model.addAttribute("user", u);		
		return "editProfile";		
	}
	
		
	

}
