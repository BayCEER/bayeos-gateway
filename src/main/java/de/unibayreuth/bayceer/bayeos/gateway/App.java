package de.unibayreuth.bayceer.bayeos.gateway;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import de.unibayreuth.bayceer.bayeos.gateway.repo.DomainFilteredRepositoryImpl;


@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass=DomainFilteredRepositoryImpl.class)
public class App {	

	@Value("${project.version}")
	String version;
	
	@Value("${build.year}")
	String year;
	 
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.setBasename("messages");
		resourceBundleMessageSource.setDefaultEncoding("UTF-8");				
		Properties p = new Properties();
		p.setProperty("project.version", version);				
		p.setProperty("build.year", year);
		resourceBundleMessageSource.setCommonMessages(p);		
		return resourceBundleMessageSource;
	}
		
}
