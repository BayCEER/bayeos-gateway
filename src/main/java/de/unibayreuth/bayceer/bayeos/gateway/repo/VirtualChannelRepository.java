package de.unibayreuth.bayceer.bayeos.gateway.repo;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel;

public interface VirtualChannelRepository extends PagingAndSortingRepository<VirtualChannel, Long> {
		
	VirtualChannel findFirstByOrderByNrDesc();
	List<VirtualChannel> findByBoardId(Long id);
	
}
