package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.ChannelTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.FunctionRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.IntervalRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.SplineRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UnitRepository;

@Controller
public class ChannelTemplateController extends AbstractController {
	
	@Autowired 
	ChannelTemplateRepository repoChannel;
	
	@Autowired
	BoardTemplateRepository repoBoard;
	
	@Autowired
	IntervalRepository repoInterval;
	
	@Autowired
	FunctionRepository repoFunction;
	
	@Autowired
	SplineRepository repoSpline;
	
	@Autowired
	UnitRepository repoUnit;
	
	
	@RequestMapping(path="/channelTemplates/create/{id}",method=RequestMethod.GET)
	public String create(@PathVariable Long id, Model model){
		BoardTemplate bt = repoBoard.findOne(userSession.getUser(),id);		
		if (bt == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(bt);		
		ChannelTemplate ct = new ChannelTemplate();		
		bt.addTemplate(ct);		
		ct.setNr(String.valueOf(bt.getTemplateCount()));		
		model.addAttribute("channelTemplate",ct);
		model.addAttribute("intervals",repoInterval.findAll(userSession.getUser(),null));
		model.addAttribute("functions",repoFunction.findAll(userSession.getUser(),null));
		model.addAttribute("splines",repoSpline.findAll(userSession.getUser(),null));
		model.addAttribute("units",repoUnit.findAll(userSession.getUser(),null));
		return "editChannelTemplate";
	}
	
	@RequestMapping(value="/channelTemplates/save", method=RequestMethod.POST)
	public String save(@Valid ChannelTemplate cha, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editChannelTemplate";
		}	
		checkWrite(repoBoard.findOne(cha.getBoardTemplate().getId()));
		repoChannel.save(cha);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/boardTemplates/" + cha.getBoardTemplate().getId();
	}
	
	@RequestMapping(path="/channelTemplates/{id}",method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
			ChannelTemplate t = repoChannel.findOne(id);
			if (t == null) throw new EntityNotFoundException("Entity not found");
			checkRead(t.getBoardTemplate());
			model.addAttribute("channelTemplate", t);
			model.addAttribute("intervals",repoInterval.findAll(userSession.getUser(),null));
			model.addAttribute("functions",repoFunction.findAll(userSession.getUser(),null));
			model.addAttribute("splines",repoSpline.findAll(userSession.getUser(),null));
			model.addAttribute("units",repoUnit.findAll(userSession.getUser(),null));
			return "editChannelTemplate";
	}
		
	
	@RequestMapping(path="/channelTemplates/delete/{id}")
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {		
		ChannelTemplate t = repoChannel.findOne(id);
		if (t == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(t.getBoardTemplate());
		repoChannel.delete(id);
		redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;		
		return "redirect:/boardTemplates/" + t.getBoardTemplate().getId();
	}
	
	
	
	
	
	
	
}
