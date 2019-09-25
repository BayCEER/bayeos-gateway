package de.unibayreuth.bayceer.bayeos.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.unibayreuth.bayceer.bayeos.gateway.repo.DomainRepository;

@Controller
public class LoginController {
	
	@Autowired
	DomainRepository repoDomain;
	
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, @CookieValue(value = "DOMAIN", defaultValue = "NULL") String domain) {
    	model.addAttribute("domains",repoDomain.findAll());    	
    	model.addAttribute("domain",domain);
        return "login";
    }

}
