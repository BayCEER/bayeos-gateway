package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
		
	@Autowired
	HandlerInterceptor controllerNameInterceptor;
	
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(controllerNameInterceptor);				
	}
	
	
	@Override 	
	public void addFormatters(FormatterRegistry registry) {	
		registry.addFormatter(new TimeZoneFormatter());		
	}
	
		
}


