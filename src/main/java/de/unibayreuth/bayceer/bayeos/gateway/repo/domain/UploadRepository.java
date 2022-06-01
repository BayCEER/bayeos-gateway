package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Upload;


@Repository
public interface UploadRepository extends DomainEntityRepository<Upload>{
		
	@Query("select u from Upload u where u.importStatus = 2 order by id asc")
	List<Upload> findPending();
	
	@Query("select u from Upload u where u.importStatus = 0 and u.importTime <= :importTime")
	List<Upload> findExpired( @Param("importTime") Date importTime);
	
	
}
