package de.unibayreuth.bayceer.bayeos.gateway.service;

import org.springframework.transaction.annotation.Transactional;

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;


public interface BoardTemplateService {
	
	@Transactional
	public void save(BoardTemplate s);
	
	@Transactional
	public void persist(BoardTemplate s);

}
