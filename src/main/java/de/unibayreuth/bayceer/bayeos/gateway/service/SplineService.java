package de.unibayreuth.bayceer.bayeos.gateway.service;

import org.springframework.transaction.annotation.Transactional;

import de.unibayreuth.bayceer.bayeos.gateway.model.SplineWebFlow;


public interface SplineService {

	@Transactional
	public void persist(SplineWebFlow spline);
}
