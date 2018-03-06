package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

public abstract class AbstractCRUDController {	
	
	@Autowired
    private MessageSource msg;
	
	protected String msgEntity;
	
	public AbstractCRUDController() {
			String c = this.getClass().getSimpleName();
			String name = c.substring(0, c.length() - 10);			
			msgEntity = "e." + Character.toLowerCase(name.charAt(0)) + name.substring(1);		
	}
		
	protected String getActionMsg(String action, Locale locale){
		return msg.getMessage(msgEntity, null, locale) + ' ' + msg.getMessage("action." + action, null, locale) + ".";
	}

}
