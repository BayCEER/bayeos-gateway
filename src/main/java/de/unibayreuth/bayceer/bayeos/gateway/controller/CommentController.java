package de.unibayreuth.bayceer.bayeos.gateway.controller;


import java.security.Principal;
import java.util.Locale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Comment;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.CommentRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.UserRepository;

@Controller
public class CommentController extends AbstractCRUDController {
	
	@Autowired
	CommentRepository repo;
	
	@Autowired 
	BoardRepository repoBoard;
	
	@Autowired
	UserRepository repoUser;
		
	@RequestMapping(value="/comments/{id}", method=RequestMethod.GET)
	public String list(final @PathVariable Long id, Model model, @SortDefault(direction=Direction.DESC,sort="insertTime") Pageable pageable){		
		Specification<Comment> spec = new Specification<Comment>() {
			@Override
			public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("board").get("id"),id);
			}
		};
		model.addAttribute("comments",  repo.findAll(spec,pageable));
		model.addAttribute("board", id);
		return "listComments";
	}
	
	
	@RequestMapping(value="/comments/create/{id}", method=RequestMethod.GET)
	public String create(@PathVariable Long id, Model model){		
		model.addAttribute("comment", new Comment());
		model.addAttribute("board", id);
		return "editComment";
	}
	
	@RequestMapping(value="/comments/edit/{id}")
	public String edit(@PathVariable Long id, Model model){
		Comment c = repo.findOne(id);
		model.addAttribute("comment",c);
		model.addAttribute("board",c.getBoard().getId());
		return "editComment";
		
	}
	
	@RequestMapping(value="/comments/save", method=RequestMethod.POST)
	public String save(@Valid Comment com, @ModelAttribute("board") Long id, RedirectAttributes redirect, Locale locale, Principal principal){		 
		com.setBoard(repoBoard.findOne(id));
		com.setUser(repoUser.findByUserName(principal.getName()));		
		repo.save(com);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));		
		return "redirect:/comments/" + id;
	}
	
	
	@RequestMapping(value="/comments/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id){		
		Comment c = repo.findOne(id);
		Long bId = c.getBoard().getId();
		repo.delete(c);
		return "redirect:/comments/" + bId;
	}
	
	
	
		
}
