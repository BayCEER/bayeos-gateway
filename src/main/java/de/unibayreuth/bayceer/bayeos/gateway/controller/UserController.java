package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Role;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;

@Controller
public class UserController extends AbstractController {
	
	@Autowired 
	UserRepository repo;
	
	@RequestMapping(value="/users/create", method=RequestMethod.GET)
	public String create(Model model){
		User u = new User();
		u.setDomain(userSession.getUser().getDomain());
		model.addAttribute("user",u);	
		model.addAttribute("action","create");
		return "editUser";		
	}
			
		
	@RequestMapping(value="/users/save", method=RequestMethod.POST)
	public String save(@Valid User user, BindingResult bindingResult, RedirectAttributes redirect, Locale locale, Model model){
		if (bindingResult.hasErrors()){
			model.addAttribute("action",(user.getId()==null)?"create":"edit");
			return "editUser";
		}					
		if (user.newPassword()){
			user.encodeNewPassword();			
		} else {
			// Re-fetch old password			
			User u = repo.findOne(user.getId());
			user.setPassword(u.getPassword());						
		}
		repo.save(user);		
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/users";
	}
	
			
	@RequestMapping(value="/users", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){										
		model.addAttribute("users", repo.findAll(userSession.getUser(),domainFilter,pageable));											
		return "listUser";
	}
	
	
	@RequestMapping(value="/users/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		User u = repo.findOne(id);				 
		u.setPassword(null);
		model.addAttribute("user",u);
		model.addAttribute("action","edit");
		return "editUser";		
	}
	
		
	@RequestMapping(value="/users/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(id);		
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/users";
	}
	
	@ModelAttribute("allRoles")
	public List<Role> populateFeatures() {
	    return Arrays.asList(Role.ALL);
	}

}
