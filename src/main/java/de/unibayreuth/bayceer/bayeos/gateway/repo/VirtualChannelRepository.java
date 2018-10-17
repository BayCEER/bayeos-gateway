package de.unibayreuth.bayceer.bayeos.gateway.repo;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel;

public interface VirtualChannelRepository extends DataTablesRepository<VirtualChannel, Long> {
		
	VirtualChannel findFirstByOrderByNrDesc();
	
}
