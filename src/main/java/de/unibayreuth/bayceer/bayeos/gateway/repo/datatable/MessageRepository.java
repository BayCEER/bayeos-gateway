package de.unibayreuth.bayceer.bayeos.gateway.repo.datatable;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import de.unibayreuth.bayceer.bayeos.gateway.model.Message;


public interface MessageRepository extends  DataTablesRepository<Message, Long> {



}

