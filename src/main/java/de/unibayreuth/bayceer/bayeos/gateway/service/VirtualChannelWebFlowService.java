package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.util.ArrayList;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelBinding;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelFunction;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannelWebFlow;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannelWebFlow.Binding;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.VirtualChannelRepository;

@Service
public class VirtualChannelWebFlowService {
	
	@Autowired
	VirtualChannelRepository repoVC;
	
	@Autowired
	BoardRepository repoBoard;
	
	
	@Autowired
	public UserSession userSession;
		
	
	public void create(Long boardId, VirtualChannelWebFlow wf, ChannelFunction f){				
		Board b = repoBoard.findOne(userSession.getUser(),boardId);
		if (b == null) throw new EntityNotFoundException("Failed to load board data."); 
		VirtualChannel c = new VirtualChannel();		
		c.setChannelFunction(f);
		c.setNr(wf.getNr());
		c.setBoard(b);
		c.setChannelBindings(new ArrayList<ChannelBinding>());
		for(int i=0;i<wf.getBindings().size();i++){
			Binding w = wf.getBindings().get(i); 
			ChannelBinding cb = new ChannelBinding();			
			cb.setNr(w.getNr());
			cb.setValue(w.getValue());
			cb.setParameter(f.getParameters().get(i));
			cb.setVirtualchannel(c);
			c.getChannelBindings().add(cb);						
		}		
		repoVC.save(c);		
	}

	
}
