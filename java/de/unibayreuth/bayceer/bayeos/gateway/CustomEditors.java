package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CustomEditors {	
	@InitBinder
	public void registerCustomEditors(WebDataBinder binder, WebRequest req) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

}
