package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Channel;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.ChannelRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.FunctionRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.IntervalRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.SplineRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UnitRepository;

@Controller
public class ChannelController extends AbstractController {
	
	@Autowired
	ChannelRepository repo;	
	@Autowired
	BoardRepository repoBoard;
	@Autowired
	IntervalRepository repoInterval;	
	@Autowired
	FunctionRepository repoFunction;	
	@Autowired
	SplineRepository repoSpline;	
	@Autowired
	UnitRepository repoUnit;
	
	@RequestMapping(path ="/channels/toggleNagios/{id}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void toggleNagios(@PathVariable Long id, @RequestParam Boolean enabled){				
		Channel c = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());
		if (c == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(c.getBoard());		
		c.setNagios(enabled);		
		repo.save(c);		
	}
	
	@RequestMapping(value="/channels/save", method=RequestMethod.POST)
	public String save(@Valid Channel channel, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editUser";
		}						
		checkWrite(repoBoard.findById(channel.getBoard().getId()).orElseThrow(() -> new EntityNotFoundException()));
		Channel c = repo.save(channel);		
		redirect.addFlashAttribute("globalMessage",getActionMsg("saved", locale));
		return "redirect:/boards/" + c.getBoard().getId();			
	}
	
	@RequestMapping(path ="/channels/{id}", method = RequestMethod.GET)		
	public String edit(@PathVariable Long id, Model model){		
		Channel c = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());
		if (c == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(c.getBoard());
		model.addAttribute("channel",c);		
		model.addAttribute("intervals",repoInterval.findAll(userSession.getUser(),null));		
		model.addAttribute("functions",repoFunction.findAll(userSession.getUser(),null));		
		model.addAttribute("splines",repoSpline.findAll(userSession.getUser(),null));		
		model.addAttribute("units",repoUnit.findAll(userSession.getUser(),null));
		return "editChannel";	
	}

	
	@RequestMapping(path ="/channels/delete/{id}", method = RequestMethod.GET)
	public String delete(@PathVariable Long id, RedirectAttributes redirect, Locale locale){		
		Channel c = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());
		if (c == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(c.getBoard());
		repo.delete(c);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/boards/" + c.getBoard().getId();				
	}
	
}
