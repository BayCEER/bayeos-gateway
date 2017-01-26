package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.unibayreuth.bayceer.bayeos.gateway.model.Message;


public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor {
	
	

}

