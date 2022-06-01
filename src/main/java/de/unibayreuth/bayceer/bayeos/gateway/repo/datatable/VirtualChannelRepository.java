package de.unibayreuth.bayceer.bayeos.gateway.repo.datatable;

import java.util.List;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel;

public interface VirtualChannelRepository extends DataTablesRepository<VirtualChannel, Long> {
		
	VirtualChannel findFirstByOrderByNrDesc();
	List<VirtualChannel> findByBoardId(Long id);
	
}
