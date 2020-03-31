package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Upload;


@Repository
public interface UploadRepository extends DomainFilteredRepository<Upload>{
	
	List<Upload> findByImportTimeIsNullOrderByIdAsc();
	
	@Query("select u from Upload u where u.importTime <= :importTime")
	List<Upload> findExpired( @Param("importTime") Date importTime);
	
	
}
