package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.ldap.LdapAuthenticationProvider;
import de.unibayreuth.bayceer.bayeos.gateway.ldap.LdapRestController;
import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.Role;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.ContactRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UserRepository;

@Controller
public class UserController extends AbstractController {
	
	@Autowired 
	UserRepository repo;
	
	@Autowired
	ContactRepository repoContact;
	
	@Autowired
	LdapAuthenticationProvider ldapAuthenticationProvider;
	
	@Autowired
	LdapRestController ldapSearchController;
		
	
	@RequestMapping(value="/users/create", method=RequestMethod.GET)
	public String create(Model model){
		User u = new User();
		u.setDomain(userSession.getUser().getDomain());
		model.addAttribute("user",u);			
		return "editUser";		
	}
	
	
	@RequestMapping(value="/users/create", method=RequestMethod.POST)
	public String createWithName(String firstName, String lastName, String userName, Model model){
		User u = new User();
		u.setDomain(userSession.getUser().getDomain());
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setUserName(userName);		
		model.addAttribute("user",u);			
		return "editUser";		
	}
	
	
			
		
	@RequestMapping(value="/users/save", method=RequestMethod.POST)
	public String save(@Valid User user, BindingResult bindingResult, RedirectAttributes redirect, Locale locale, Model model){
		if (bindingResult.hasErrors()){			
			return "editUser";
		}
		
		if (user.getNewPassword()!=null) {
			user.encodeNewPassword();		
		}  else if (user.getId() != null) {				
			user.setPassword(repo.findById(user.getId()).orElseThrow(()-> new EntityNotFoundException()).getPassword());
		}
														
		if (user.getContact().getEmail() == null) {						 
			user.setContact(null);									
		} else {					
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
		}																
		repo.save(userSession.getUser(),user);		
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/users";
	}
	
			
	@RequestMapping(value="/users", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){										
		model.addAttribute("users", repo.findAll(userSession.getUser(),domainFilter,pageable));											
		return "listUser";
	}
	
	
	
	@RequestMapping(value="/users/editPassword/{id}", method=RequestMethod.GET)
	public String editPassword(@PathVariable Long id , Model model) {
		User u = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());				 
		u.setPassword(null);
		model.addAttribute("user",u);
		return "editPassword";
	}
	
	@RequestMapping(value="/users/savePassword", method=RequestMethod.POST)
	public String savePassword(@Valid User user, BindingResult bindingResult, RedirectAttributes redirect, Locale locale, Model model){
		if (bindingResult.hasErrors()){
			return "editPassword";
		}		
		User u = repo.findOne(userSession.getUser(), user.getId());		
		if (user.getNewPassword()!=null) {
			user.encodeNewPassword();
			u.setPassword(user.getPassword());
		} else {
			u.setPassword(null);
		}
		
		repo.save(userSession.getUser(),u);		
		redirect.addFlashAttribute("globalMessage", getMsg("login.password", locale) + " " + getMsg("action.saved", locale));
		return "redirect:/users";
	}
	
	
	@RequestMapping(value="/users/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		User u = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());				 
		u.setPassword(null);
		model.addAttribute("user",u);		
		return "editUser";		
	}
	
	
		
		
	@RequestMapping(value="/users/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);		
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/users";
	}
	
	
	
	@ModelAttribute("allRoles")
	public List<Role> populateFeatures() {
		return Arrays.asList(Role.ALL);
	}
	
	@PostMapping(value="/users/search")
	public String search(@Valid User u, Model model) {		
		model.addAttribute("user",u);
		return "searchUser";
	}
		
		
}
