package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.DomainEntityRepositoryImpl;

@Configuration
@EnableJpaRepositories(basePackages = "de.unibayreuth.bayceer.bayeos.gateway.repo.domain",repositoryBaseClass=DomainEntityRepositoryImpl.class)
public class DomainEntityConfig {

}
