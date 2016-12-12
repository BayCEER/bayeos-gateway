package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long>{

	User findByUserName(String userName);	
	

}
