package de.unibayreuth.bayceer.bayeos.gateway;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;

@Configuration
public class ThymLeafDialect {
	
	@Autowired
	private  SpringTemplateEngine templateEngine;
	
	
	@PostConstruct
	public void extension() {
		templateEngine.addDialect(new SpringDataDialect());
	}

}
