package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardDTO;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardGroup;
import de.unibayreuth.bayceer.bayeos.gateway.model.Channel;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelDTO;
import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.Notification;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.NotificationRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardGroupRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.ContactRepository;

@Controller
public class BoardGroupController extends AbstractController {
	
	@Autowired 
	BoardGroupRepository  repo;
	
	@Autowired 
	BoardRepository repoBoard;
	
	@Autowired
	NotificationRepository repoNotification;
	
	@Autowired
	ContactRepository repoContacts;
	
	
	@RequestMapping(value="/groups/create",method=RequestMethod.GET)
	public String create(Model model){		
		BoardGroup g = new BoardGroup();
		g.setDomain(userSession.getUser().getDomain());		
		model.addAttribute("group",g);
		return "createBoardGroup";
	}
	
	@RequestMapping(value="/groups/save", method=RequestMethod.POST)
	public String save(@Valid BoardGroup group, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editBoardGroup";
		}				
		repo.save(userSession.getUser(),group);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/groups";
	}
	
	@RequestMapping(value="/groups",method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){		
		model.addAttribute("groups", repo.findAll(userSession.getUser(),domainFilter, pageable));
		return "listBoardGroup";
	}
	
	
	
	@RequestMapping(value="/groups/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model,@RequestParam(defaultValue = "boards") String tab, @Qualifier("boards") Pageable boardPage){	
		model.addAttribute("group",repo.findOne(userSession.getUser(),id));
		model.addAttribute("tab", tab);
		return "editBoardGroup";		
	}
	
	@RequestMapping(value="/groups/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/groups";
	}
	
	@RequestMapping(value="/groups/selectBoards/{id}", method=RequestMethod.GET)
	public String selectBoards(@PathVariable Long id, Model model){		
		BoardGroup g = repo.findOne(userSession.getUser(),id);
		model.addAttribute("parent",g);
		model.addAttribute("controller","groups");	
	
		List<Board> boards = repoBoard.findByBoardGroupIsNullAndDomain(g.getDomain());
		model.addAttribute("boards", boards);		
		return "selectBoards";
	}
	
	@RequestMapping(value="/groups/addBoards", method=RequestMethod.POST)	
	public String addBoards(@RequestParam("id") Long id, @RequestParam("boards") List<Long> boards){			
		BoardGroup s = repo.findOne(userSession.getUser(),id);			
		for(Long i:boards){
				Board b = repoBoard.findOne(userSession.getUser(),i);
				b.setBoardGroup(s);
				repoBoard.save(userSession.getUser(),b);								
		}		
		return "redirect:/groups/" + s.getId();
	}
	
	@RequestMapping(value="/groups/removeBoard/{id}",method=RequestMethod.GET)
	public String removeBoard(@PathVariable("id") Long id){
		Board b = repoBoard.findOne(userSession.getUser(),id);
		Long g = b.getBoardGroup().getId();
		b.setBoardGroup(null);
		repoBoard.save(userSession.getUser(),b);
		return "redirect:/groups/" + g;
	}
	
	
	@RequestMapping(value="/groups/selectContacts/{id}", method=RequestMethod.GET)
	public String selectContacts(@PathVariable Long id, Model model) {
		BoardGroup g = repo.findOne(userSession.getUser(),id);
		model.addAttribute("parent",g);
		model.addAttribute("controller","groups");
		List<Contact> cs = repoContacts.findAll(userSession.getUser(),domainFilter);
		model.addAttribute("contacts", cs);		
		return "selectContacts";
	}
	
	@RequestMapping(value="/groups/addContacts", method=RequestMethod.POST)
	public String addContacts(@RequestParam("id") Long id,  @RequestParam("contacts") List<Long> contacts) {
		BoardGroup g = repo.findOne(userSession.getUser(),id);			
		for(Long i:contacts){
			Contact c = repoContacts.findOne(userSession.getUser(),i);
			if (c!=null) {
				Notification n = new Notification();
				n.setBoardGroup(g);				
				n.setContact(c);				
				repoNotification.save(n);
			}										
		}		
		return "redirect:/groups/" + g.getId() + "?tab=notifications";		
	}
			
		
	@RequestMapping(value="/groups/removeNotification/{id}", method = RequestMethod.GET)
	public String removeNotification(@PathVariable("id") Long id) {		
		Notification n = repoNotification.findById(id).orElseThrow(()-> new EntityNotFoundException());
		checkWrite(n.getBoardGroup());
		Long g = n.getBoardGroup().getId();		
		repoNotification.deleteById(n.getId());	
		return "redirect:/groups/" + g + "?tab=notifications";
	}
	
	
	@RequestMapping(value="/groups/createNewContact/{id}", method=RequestMethod.GET)
	public String createNewContact(@PathVariable Long id, Model model){
		BoardGroup b = repo.findOne(userSession.getUser(),id);		
		Contact c = new Contact();					
		c.setDomain(b.getDomain());
		model.addAttribute("parent",b);
		model.addAttribute("controller","groups");						
		model.addAttribute("contact",c);
		return "createNewContact";
	}
	
	@RequestMapping(value="/groups/addNewContact", method=RequestMethod.POST)
	public String addNewContact(@Valid Contact contact, Long id) {				
		BoardGroup b = repo.findOne(userSession.getUser(), id);		
		if (b != null) {
			Contact c = repoContacts.findOneByEmailAndDomain(contact.getEmail(),b.getDomain());
			if (c == null) {
				contact.setDomain(b.getDomain());
				c = repoContacts.save(contact);				
			} 			
			Notification n = new Notification();
			n.setBoardGroup(b);
			n.setContact(c);
			repoNotification.save(n);			
		}										
		return "redirect:/groups/" + id + "?tab=notifications";
								
	}
	
	@RequestMapping(value="/groups/map/{id}", method=RequestMethod.GET)
    public String showMap(@PathVariable Long id , Model model, Locale locale) {
	    BoardGroup g = repo.findOne(userSession.getUser(),id);
	    model.addAttribute("name",getMsg("e.boardGroup", locale) + ' ' + g.getName());	    
	    model.addAttribute("boards",g.getBoards());
	    return "mapBoards";
    }
	
	
	
	
	
	
	
	
	
}
