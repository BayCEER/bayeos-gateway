package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint;
import de.unibayreuth.bayceer.bayeos.gateway.model.Point;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.model.SplineWebFlow;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.DomainRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.SplineRepository;

@Service("splineService")
public class SplineServiceImpl implements SplineService, Serializable {

	@Autowired
	SplineRepository repoSpline;
	@Autowired
	DomainRepository repoDomain;
	
	
	@Autowired
	public UserSession userSession; 
	
	private static final long serialVersionUID = -2186894760876637871L;
	
	public void persist(SplineWebFlow s) {		
		Domain d = s.getDomain();		
		if (!d.getName().isEmpty()) {			
			s.setDomain(repoDomain.findOneByName((d.getName())));
		} else {
			s.setDomain(null);
		}		
		Spline sp = new Spline();
		sp.setName(s.getName());		
		for (Point p : s.getPoints()) {		
			sp.addKnotPoint(new KnotPoint(p.getX(), p.getY()));
		}		
		repoSpline.save(sp);
	}

}
