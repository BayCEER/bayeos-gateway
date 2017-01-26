package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.repo.SplineRepository;

@Service("splineService")
public class SplineServiceImpl implements SplineService, Serializable {

	@Autowired
	SplineRepository repoSpline;
	private static final long serialVersionUID = -2186894760876637871L;
	
	public void persist(Spline spline) {		
		repoSpline.save(spline);
	}

}
