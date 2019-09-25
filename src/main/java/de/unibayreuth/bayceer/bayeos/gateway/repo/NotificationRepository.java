package de.unibayreuth.bayceer.bayeos.gateway.repo;


import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Notification;

public interface NotificationRepository extends DataTablesRepository<Notification,Long> {
	
 
}
