package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;

public interface DomainRepository extends DataTablesRepository<Domain, Long> {
	 List<Domain> findAllById(Long id, Pageable pageable);
	 Domain findOneByName(String name);
	
}
