package de.unibayreuth.bayceer.bayeos.gateway;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public class CustomSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
		// Add a domain name cookie
		User u = (User)authentication.getPrincipal();				
		Cookie cookie = new Cookie("DOMAIN", (u.getDomain()==null) ? "NULL":u.getDomain().getName());
		cookie.setMaxAge(2592000);		
		response.addCookie(cookie);	
		super.onAuthenticationSuccess( request, response, authentication );
	}

}
