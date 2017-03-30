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
import de.unibayreuth.bayceer.bayeos.gateway.repo.ChannelFunctionParameterRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.ChannelFunctionRepository;

@Controller
public class ChannelFunctionController extends AbstractCRUDController{
	
	@Autowired
	ChannelFunctionRepository repo;
	
	@Autowired
	ChannelFunctionParameterRepository repoParam;
	
	
	@RequestMapping(value="/channelFunctions/create", method=RequestMethod.GET)
	public String create(Model model){
		model.addAttribute("channelFunction", new ChannelFunction());		
		return "editChannelFunction";
	}
		
	@RequestMapping(value="/channelFunctions",  params={"save"}, method=RequestMethod.POST)
	public String save(final ChannelFunction channelFunction, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editChannelFunction";
		}		
		for(ChannelFunctionParameter p: channelFunction.getParameters()){
			p.setChannelFunction(channelFunction);
		}
		repo.save(channelFunction);
		redirect.addFlashAttribute("globalMessage",getActionMsg("saved", locale));
		return "redirect:/channelFunctions";
	}
	
	@RequestMapping(value="/channelFunctions",  params={"addParam"}, method=RequestMethod.POST)
	public String addParam(final ChannelFunction channelFunction){
		channelFunction.getParameters().add(new ChannelFunctionParameter());
		return "editChannelFunction";
	}
	
	@RequestMapping(value="/channelFunctions", params={"removeParam"}, method=RequestMethod.POST)	
	public String removeParam(ChannelFunction channelFunction, @RequestParam Integer removeParam){		
		ChannelFunctionParameter cfp = channelFunction.getParameters().get(removeParam.intValue());		
		if (cfp.getId() != null){			
			repoParam.delete(cfp.getId());			
		}	
		channelFunction.getParameters().remove(removeParam.intValue());
		return "editChannelFunction";
	}
	
	
	@RequestMapping(value="/channelFunctions", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){	
		model.addAttribute("channelFunctions", repo.findAll(pageable));
		return "listChannelFunctions";
	}
	
	@RequestMapping(value="/channelFunctions/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){		
		model.addAttribute("channelFunction",repo.findOne(id));
		return "editChannelFunction";	
	}
					
	@RequestMapping(value="/channelFunctions/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;		
		return "redirect:/channelFunctions";
	}
	
	
	
	

	
	
	
}
