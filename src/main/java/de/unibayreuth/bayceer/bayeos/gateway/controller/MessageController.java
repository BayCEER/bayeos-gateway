package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.util.Locale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.Message;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.MessageRepository;

@Controller
public class MessageController extends AbstractCRUDController{
	
	@Autowired
	MessageRepository repo;
	
	@Autowired
	BoardRepository repoBoard;
		
		
	@RequestMapping(value="/messages/{id}", method=RequestMethod.GET)
	public String list(@PathVariable Long id, Model model, @SortDefault(direction=Direction.DESC,sort="insertTime") Pageable pageable){		
		final Board b = repoBoard.findOne(id);		
		Specification<Message> spec = new Specification<Message>() {
			@Override
			public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("origin"), b.getOrigin());
			}
		};
		model.addAttribute("messages", repo.findAll(spec,pageable));	
		model.addAttribute("origin", b.getOrigin());
		return "listMessages";
	}
	
					
	@RequestMapping(value="/messages/delete/{id}", method=RequestMethod.GET)	
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {		
		Message m = repo.findOne(id);		
		Board b = repoBoard.findByOrigin(m.getOrigin());
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;		
		return "redirect:/messages/" + b.getId();
	}

}
