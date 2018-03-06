package de.unibayreuth.bayceer.bayeos.gateway;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class App {	

	@Value("${project.version}")
	String version;
	 
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
		resourceBundleMessageSource.setCommonMessages(p);		
		return resourceBundleMessageSource;
	}
		
}
