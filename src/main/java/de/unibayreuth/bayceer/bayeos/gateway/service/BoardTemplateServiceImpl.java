package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.util.Date;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.Channel;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.model.Unit;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.DomainRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.FunctionRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.IntervalRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.SplineRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UnitRepository;

@Service
public class BoardTemplateServiceImpl implements BoardTemplateService {
	
	@Autowired
	DomainRepository repoDomain;
	@Autowired
	BoardTemplateRepository repoTemplate;		
	@Autowired
	IntervalRepository repoInt;
	@Autowired 
	FunctionRepository repoFunc;
	@Autowired
	UnitRepository repoUnit;
	@Autowired
	SplineRepository repoSpline;
	@Autowired
	BoardRepository repoBoard;
	
	@Autowired
	UserSession userSession;
	
		
	@Override
	@Transactional
	public BoardTemplate save(BoardTemplate s) {	
		
				
		for(ChannelTemplate t: s.getTemplates()){
								
			Function f = t.getAggrFunction();
			if (f != null){											
				Function ft = repoFunc.findOneByName(userSession.getUser(), f.getName());				
				if (ft != null) {
					t.setAggrFunction(ft);
				} else {					
					f.setDomain(userSession.getDomain());
					t.setAggrFunction(repoFunc.save(userSession.getUser(),f));					
				}								
			}
			
			Interval i = t.getAggrInterval();
			if (i != null){											
				Interval it = repoInt.findOneByName(userSession.getUser(), i.getName());				
				if (it != null) {
					t.setAggrInterval(it);
				} else {
					i.setDomain(userSession.getDomain());
					t.setAggrInterval(repoInt.save(userSession.getUser(),i));					
				}								
			}
			Spline sp = t.getSpline();
			if (sp != null){											
				Spline st = repoSpline.findOneByName(userSession.getUser(), sp.getName());				
				if (st != null) {
					t.setSpline(st);
				} else {
					sp.setDomain(userSession.getDomain());
					t.setSpline(repoSpline.save(userSession.getUser(),sp));					
				}								
			}			
			Unit u = t.getUnit();
			if (u != null){											
				Unit ut = repoUnit.findOneByName(userSession.getUser(),u.getName());				
				if (ut != null) {
					t.setUnit(ut);
				} else {
					u.setDomain(userSession.getDomain());
					t.setUnit(repoUnit.save(userSession.getUser(),u));					
				}								
			}								
		}		
		return repoTemplate.save(userSession.getUser(),s);
		
	}

	@Override
	public void persist(BoardTemplate bt) {		
		// Custom handling due to wrong binding in field name with spring web flow  
		// "" empty String -> null
		// "ID" -> id
		
		Domain d = bt.getDomain();		
		if (!d.getName().isEmpty()) {			
			bt.setDomain(repoDomain.findById(Long.valueOf(d.getName())).orElseThrow(()-> new EntityNotFoundException()));
		} else {
		    bt.setDomain(null);
		}
			
		for (ChannelTemplate t: bt.getTemplates()){			
			Unit u = t.getUnit();			
			if (u.getName().isEmpty()){
				t.setUnit(null);
			} else {
				t.setUnit(repoUnit.findOne(userSession.getUser(),Long.valueOf(u.getName())));
			}						
			Spline s = t.getSpline();
			if (s.getName().isEmpty()){
				t.setSpline(null);
			} else {
				t.setSpline(repoSpline.findOne(userSession.getUser(),Long.valueOf(s.getName())));
			}			
			Interval i = t.getAggrInterval();			
			if (i.getName().isEmpty()){
				t.setAggrInterval(null);
			} else {
				t.setAggrInterval(repoInt.findOne(userSession.getUser(),Long.valueOf(i.getName())));
			}			
			Function f = t.getAggrFunction();
			if (f.getName().isEmpty()){
				t.setAggrFunction(null); 				
			} else {
				t.setAggrFunction(repoFunc.findOne(userSession.getUser(),Long.valueOf(f.getName())));
			}									
		}	
		repoTemplate.save(userSession.getUser(),bt);		
	}

	@Override
	public BoardTemplate saveAsTemplate(Long boardId) {		
		Board b = repoBoard.findOne(userSession.getUser(),boardId);						
		BoardTemplate t = new BoardTemplate();
		t.setName("New Template");
		t.setDescription("");
		BeanUtils.copyProperties(b, t, new String[]{"id","name"});						
		for(Channel c:b.getChannels()){			
			ChannelTemplate ct = new ChannelTemplate();
			BeanUtils.copyProperties(c, ct, new String[]{"id"});	
			if (ct.getName() == null){
				ct.setName("Channel " + ct.getNr());
			}
			t.addTemplate(ct);
		}		
		return save(t);
		
	}

	@Override
	public void applyTemplate(Long boardId, Long templateId) {		
		Board b = repoBoard.findOne(userSession.getUser(),boardId);		
		BoardTemplate t = repoTemplate.findOne(userSession.getUser(),templateId);
		BeanUtils.copyProperties(t, b, new String[]{"id","name","domain"});		
		for(ChannelTemplate ct:t.getTemplates()){					
			Channel c = b.findOrCreateChannel(ct.getNr());			
			BeanUtils.copyProperties(ct, c, new String[]{"id"});
			
		}
		t.setLastApplied(new Date());
		repoTemplate.save(t);
		repoBoard.save(userSession.getUser(),b);	
	}
}
