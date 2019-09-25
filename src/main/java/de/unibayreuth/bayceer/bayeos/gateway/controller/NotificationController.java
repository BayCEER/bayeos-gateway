package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardGroup;
import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.Notification;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardGroupRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.ContactRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.NotificationRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;

@Controller
public class NotificationController extends AbstractController{

@Autowired
NotificationRepository repoNotification;

@Autowired
BoardRepository repoBoard;

@Autowired
UserRepository repoUser;

@Autowired
BoardGroupRepository repoGroups;

@Autowired
ContactRepository repoContacts;



private Specification<Notification> contactId(Long id) {
	return (root, query, cb) -> {
		return cb.equal(root.get("contact").get("id"),id);			
	};
}


@RequestMapping(value="/notifications", method=RequestMethod.GET)
public String list(Model model, Pageable pageable){	
	User u = repoUser.findOne(userSession.getUser().getId());	
	Contact c = u.getContact();
	if (c != null) {
		Page<Notification> n = repoNotification.findAll(contactId(c.getId()),pageable);
		model.addAttribute("notifications",n);
	}	 	
	model.addAttribute("user", u);
	return "listNotifications";
}

@RequestMapping(value="/notifications/remove/{id}", method=RequestMethod.GET)
public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {	
	Notification n = repoNotification.findOne(id);	
	if (n.getBoard()!=null) {		
		checkWrite(n.getBoard());	
	} else if (n.getBoardGroup()!=null) {
		checkWrite(n.getBoardGroup());
	}	
	repoNotification.delete(id);
	redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
	return "redirect:/notifications";
}


@RequestMapping(value="/notifications/selectBoards", method=RequestMethod.GET)
public String selectBoards(Model model) {
	model.addAttribute("parent",repoUser.findOne(userSession.getUser().getId()));
	model.addAttribute("controller","notifications");
	model.addAttribute("boards", repoBoard.findAll(userSession.getUser(),domainFilter));
	return "selectBoards";
}

@RequestMapping(value="/notifications/selectGroups", method=RequestMethod.GET)
public String selectGroups(Model model) {
	model.addAttribute("parent",repoUser.findOne(userSession.getUser().getId()));
	model.addAttribute("controller","notifications");
	model.addAttribute("groups", repoGroups.findAll(userSession.getUser(),domainFilter));
	return "selectGroups";
}

@RequestMapping(value="/notifications/addGroups", method=RequestMethod.POST)
public String addGroups(@RequestParam("id") Long id, @RequestParam("groups") List<Long> groups){			
	User u = repoUser.findOne(userSession.getUser(),id);	
	Contact c = u.getContact();	
	if (c!= null) {				
		for(Long i:groups){
			BoardGroup g = repoGroups.findOne(userSession.getUser(),i);
			if (g!= null) {
				Notification n = new Notification();
				n.setBoardGroup(g);				
				n.setContact(c);				
				repoNotification.save(n);
			}
		}		
	}			
	return "redirect:/notifications";
}




@RequestMapping(value="/notifications/addBoards", method=RequestMethod.POST)
public String addBoards(@RequestParam("id") Long id, @RequestParam("boards") List<Long> boards){			
	User u = repoUser.findOne(userSession.getUser(),id);	
	Contact c = u.getContact();	
	if (c!= null) {				
		for(Long i:boards){
			Board b = repoBoard.findOne(userSession.getUser(),i);
			if (b!= null) {
				Notification n = new Notification();
				n.setBoard(b);				
				n.setContact(c);				
				repoNotification.save(n);
			}
		}		
	}			
	return "redirect:/notifications";
}

}
