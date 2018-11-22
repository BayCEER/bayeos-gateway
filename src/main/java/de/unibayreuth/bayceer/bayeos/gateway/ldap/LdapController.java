package de.unibayreuth.bayceer.bayeos.gateway.ldap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LdapController {
	
	@GetMapping(value="/ldap/selectUser")
	public String searchUser(Model model) {		
		return "selectLdapUser";
	}

}
