package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import de.unibayreuth.bayceer.bayeos.gateway.repo.DomainFilteredRepositoryImpl;


@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass=DomainFilteredRepositoryImpl.class)
public class App {	

	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	
}
