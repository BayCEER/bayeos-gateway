package de.unibayreuth.bayceer.bayeos.gateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Bean
	public LayoutDialect layoutDialect() {
	  return new LayoutDialect();
	}
	
	@Autowired
	HandlerInterceptor controllerNameInterceptor;
	
	@Autowired 
	HandlerInterceptor domainFilterInterceptor; 
		
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(controllerNameInterceptor);	
		registry.addInterceptor(domainFilterInterceptor);
	}
		
	@Override 	
	public void addFormatters(FormatterRegistry registry) {		
		registry.addFormatter(new TimeZoneFormatter());		
	}


}


