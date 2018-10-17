package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
		
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
	
		
	@Bean
	public SpringDataDialect springDataDialect(){
		return new SpringDataDialect();
	}
	
	
		
}


