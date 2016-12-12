package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Role;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;

@Controller
public class UserController {
	
	@Autowired
	UserRepository repo;
	
	
	@RequestMapping(value="/users/create", method=RequestMethod.POST)
	public String create(Model model){
		model.addAttribute("user", new User());
		return "editUser";
	}
		
	@RequestMapping(value="/users/save", method=RequestMethod.POST)
	public String save(User user, RedirectAttributes redirect){				
		repo.save(user);
		redirect.addFlashAttribute("globalMessage", "User saved.");
		return "redirect:/users";
	}
		
	@RequestMapping(value="/users", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("userName") Pageable pageable){
		model.addAttribute("users", repo.findAll(pageable));
		return "listUser";
	}
	
	@RequestMapping(value="/users/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("user",repo.findOne(id));
		return "editUser";		
	}
	
		
	@RequestMapping(value="/users/{id}", method=RequestMethod.DELETE)
	public String delete(@PathVariable Long id , RedirectAttributes redirect) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage", "User removed successfully");
		return "redirect:/users";
	}
	
	@ModelAttribute("allRoles")
	public List<Role> populateFeatures() {
	    return Arrays.asList(Role.ALL);
	}

}
