package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

@SessionScope
@Component
public class Session implements UserSession {

	@Override
	public User getUser() {
		return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	@Override
	public Domain getDomain() {
		User u = getUser();		
		if (u != null) {
			return u.getDomain();
		} else {
			return null;
		}
	}
	

}
