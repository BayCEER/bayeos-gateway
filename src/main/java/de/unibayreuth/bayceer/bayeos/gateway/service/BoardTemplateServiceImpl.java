package de.unibayreuth.bayceer.bayeos.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.model.Unit;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.FunctionRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.IntervalRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.SplineRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UnitRepository;

@Service
public class BoardTemplateServiceImpl implements BoardTemplateService {
	
	
	@Autowired
	BoardTemplateRepository boardRepo;
		
	@Autowired
	IntervalRepository intRepo;
	
	@Autowired 
	FunctionRepository funcRepo;
	
	@Autowired
	UnitRepository unitRepo;
	
	@Autowired
	SplineRepository splineRepo;
	
	
	@Override
	@Transactional
	public void save(BoardTemplate s) {				
		for(ChannelTemplate t: s.getTemplates()){
						
			Function f = t.getAggrFunction();
			if (f != null){											
				Function ft = funcRepo.findByName(f.getName());				
				if (ft != null) {
					t.setAggrFunction(ft);
				} else {					
					t.setAggrFunction(funcRepo.save(f));					
				}								
			}
			
			Interval i = t.getAggrInterval();
			if (i != null){											
				Interval it = intRepo.findByName(i.getName());				
				if (it != null) {
					t.setAggrInterval(it);
				} else {
					t.setAggrInterval(intRepo.save(i));					
				}								
			}
			Spline sp = t.getSpline();
			if (sp != null){											
				Spline st = splineRepo.findByName(s.getName());				
				if (st != null) {
					t.setSpline(st);
				} else {
					t.setSpline(splineRepo.save(sp));					
				}								
			}			
			Unit u = t.getUnit();
			if (u != null){											
				Unit ut = unitRepo.findByName(u.getName());				
				if (ut != null) {
					t.setUnit(ut);
				} else {
					t.setUnit(unitRepo.save(u));					
				}								
			}								
		}		
		boardRepo.save(s);
		
	}

	@Override
	public void persist(BoardTemplate s) {		
		// Custom handling due to wrong conversation from 
		// "" empty String to Object using spring web flow 
		for (ChannelTemplate t: s.getTemplates()){
			if (t.getUnit().getId() == null){
				t.setUnit(null);
			}			
			if (t.getSpline().getId() == null){
				t.setSpline(null);
			}
			if (t.getAggrInterval().getId() == null){
				t.setAggrInterval(null);
			}
			if (t.getAggrFunction().getId() == null){
				t.setAggrFunction(null);
			}			
		}	
		boardRepo.save(s);		
	}

}
