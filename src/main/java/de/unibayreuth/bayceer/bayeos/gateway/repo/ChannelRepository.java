package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.repository.CrudRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Channel;

public interface ChannelRepository extends CrudRepository<Channel, Long>{
	
}

