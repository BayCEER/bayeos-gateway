package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long>{

//	// Retrieves a transient property 
//	@Query(nativeQuery=true,value="select password from users where id = ?")
//	String getHash(Long id);

	User findByUserName(String userName);	

}
