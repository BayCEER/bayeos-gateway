package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Upload;


@Repository
public interface UploadRepository extends DomainFilteredRepository<Upload>{
	List<Upload> findAllByOrderByIdAsc();	
}
