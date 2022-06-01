package de.unibayreuth.bayceer.bayeos.gateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryImpl;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "de.unibayreuth.bayceer.bayeos.gateway.repo.datatable",repositoryBaseClass=DataTablesRepositoryImpl.class)
public class DataTablesConfig {

}
