package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelFunction;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelFunctionParameter;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.ChannelFunctionParameterRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.ChannelFunctionRepository;

@Controller
public class ChannelFunctionController extends AbstractController{
	
	@Autowired
	ChannelFunctionRepository repo;
	
	@Autowired
	ChannelFunctionParameterRepository repoParam;
	
	
	@RequestMapping(value="/channelFunctions/create", method=RequestMethod.GET)
	public String create(Model model){
		ChannelFunction cf = new ChannelFunction();
		cf.setDomain(userSession.getDomain());
		model.addAttribute("channelFunction",cf);	
		model.addAttribute("writeable",isWriteable(cf));
		return "editChannelFunction";
	}
		
	@RequestMapping(value="/channelFunctions",  params={"save"}, method=RequestMethod.POST)
	public String save(Model model, ChannelFunction cf, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			model.addAttribute("channelFunction",cf);	
			model.addAttribute("writeable",isWriteable(cf));
			return "editChannelFunction";
		}		
		for(ChannelFunctionParameter p: cf.getParameters()){
			p.setChannelFunction(cf);
		}
		repo.save(userSession.getUser(),cf);
		redirect.addFlashAttribute("globalMessage",getActionMsg("saved", locale));
		return "redirect:/channelFunctions";
	}
	
	@RequestMapping(value="/channelFunctions",  params={"addParam"}, method=RequestMethod.POST)
	public String addParam(Model model, ChannelFunction cf){
		cf.getParameters().add(new ChannelFunctionParameter());
		model.addAttribute("channelFunction",cf);
		model.addAttribute("writeable",isWriteable(cf));
		return "editChannelFunction";
	}
	
	@RequestMapping(value="/channelFunctions", params={"removeParam"}, method=RequestMethod.POST)	
	public String removeParam(Model model, ChannelFunction cf, @RequestParam Integer removeParam){		
		ChannelFunctionParameter cfp = cf.getParameters().get(removeParam.intValue());		
		if (cfp.getId() != null){			
			repoParam.deleteById(cfp.getId());			
		}	
		cf.getParameters().remove(removeParam.intValue());
		model.addAttribute("channelFunction",cf);
		model.addAttribute("writeable",isWriteable(cf));
		return "editChannelFunction";
	}
	
	
	@RequestMapping(value="/channelFunctions", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){	
		model.addAttribute("channelFunctions", repo.findAll(userSession.getUser(),domainFilter,pageable));
		return "listChannelFunctions";
	}
	
	@RequestMapping(value="/channelFunctions/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		ChannelFunction f = repo.findOne(userSession.getUser(),id);
		model.addAttribute("channelFunction",f);
		model.addAttribute("writeable",isWriteable(f));
		return "editChannelFunction";	
	}
					
	@RequestMapping(value="/channelFunctions/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;		
		return "redirect:/channelFunctions";
	}
	
	
	
	

	
	
	
}
