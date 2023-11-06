package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;
import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.DomainEntity;
import de.unibayreuth.bayceer.bayeos.gateway.service.AccessService;

public abstract class AbstractController {
	
	
	@Autowired
    protected MessageSource msg;
		
	@Autowired
	public UserSession userSession;
	
	@Autowired
	public DomainFilter domainFilter;
	
	@Autowired 
	public AccessService accessService;
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
		
					
	protected String msgEntity;
	
	public AbstractController() {		
			String c = this.getClass().getSimpleName();
			String name = c.substring(0, c.length() - 10);			
			msgEntity = "e." + Character.toLowerCase(name.charAt(0)) + name.substring(1);		
	}
		
	protected String getActionMsg(String action, Locale locale){
		return msg.getMessage(msgEntity, null, locale) + ' ' + msg.getMessage("action." + action, null, locale) + ".";
	}
	
	protected String getMsg(String code, Locale locale) {
		return msg.getMessage(code, null, locale);
	}
	

	protected Domain userDomain() {
		return userSession.getUser().getDomain();
	}
	
		
	protected  void checkWrite(DomainEntity d) {
	    if (!accessService.isWriteable(d)) {
			throw new AccessDeniedException("Missing rights to save domain object");
	    }				 		
	}
	
		
	protected  void checkRead(DomainEntity d) {
	    if (!accessService.isReadable(d)) {
            throw new AccessDeniedException("Missing rights to read domain object");
	    }	    
	}
	
	public boolean isWriteable(DomainEntity d) {
	    return accessService.isWriteable(d);
	}
	 
	public boolean isReadable(DomainEntity d) {
	    return accessService.isReadable(d);
    }
    
	
	

}
