package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.VirtualChannel;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.ChannelFunctionRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.VirtualChannelRepository;

@Controller
public class VirtualChannelController extends AbstractCRUDController {

	@Autowired
	VirtualChannelRepository repo;

	@Autowired
	BoardRepository repoBoard;
	
	@Autowired
	ChannelFunctionRepository repoFunction;

	@RequestMapping(value = "/virtualChannels/save", method = RequestMethod.POST)
	public String save(@Valid VirtualChannel vc, BindingResult bindingResult, RedirectAttributes redirect,
			Locale locale) {
		if (bindingResult.hasErrors()) {
			return "editVirtualChannel";
		}
		repo.save(vc);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/boards/" + vc.getBoard().getId() + "?tab=virtualChannels";
	}
	

	@RequestMapping(value = "/virtualChannels/delete/{id}", method = RequestMethod.GET)
	public String delete(@PathVariable Long id, RedirectAttributes redirect, Locale locale) {
		VirtualChannel vc = repo.findOne(id);
		repo.delete(vc);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/boards/" + vc.getBoard().getId() + "?tab=virtualChannels";
	}

	@RequestMapping(value = "/virtualChannels/create/{id}", method = RequestMethod.GET)
	public String create(@PathVariable Long id, Model model) {
		Board b = repoBoard.findOne(id);
		VirtualChannel vc = new VirtualChannel();
		b.getVirtualChannels().add(vc);
		model.addAttribute(vc);
		model.addAttribute("channelFunctions",repoFunction.findAll());
		return "editVirtualChannel";

	}
	
	@RequestMapping(path="/virtualChannels/{id}", method = RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){
		 model.addAttribute(repo.findOne(id));
		 model.addAttribute("channelFunctions",repoFunction.findAll());
		 return "editVirtualChannel";
	}

}