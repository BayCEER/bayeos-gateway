package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelBinding;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelFunctionParameter;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannelWebFlow;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannelWebFlow.Binding;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.VirtualChannelRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;

@Service
public class VirtualChannelWebFlowService {
	
	@Autowired
	VirtualChannelRepository repoVC;
	
	@Autowired
	BoardRepository repoBoard;
	
	
	@Autowired
	public UserSession userSession;
		
	
	public void create(Long boardId, VirtualChannelWebFlow wf){				
		Board b = repoBoard.findOne(userSession.getUser(),boardId);
		if (b == null) throw new EntityNotFoundException("Failed to load board data."); 		
		VirtualChannel c = new VirtualChannel();		
		c.setChannelFunction(wf.getFunction());
		c.setNr(wf.getNr());
		c.setBoard(b);	
		List<ChannelBinding> bindings = new ArrayList<ChannelBinding>(wf.getFunction().getParameters().size());
		for (ChannelFunctionParameter cp:wf.getFunction().getParameters()) {
			ChannelBinding cb = new ChannelBinding();						
			Binding bi = wf.getBinding().get(cp.getName());			
			cb.setValue(bi.getValue());
			cb.setNr(bi.getNr());
			cb.setParameter(cp);
			cb.setVirtualchannel(c);
			bindings.add(cb);
		}								
		c.setChannelBindings(bindings);						
		repoVC.save(c);		
	}

	
}
