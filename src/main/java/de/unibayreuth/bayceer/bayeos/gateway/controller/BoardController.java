package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Collections;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardDTO;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.Channel;
import de.unibayreuth.bayceer.bayeos.gateway.model.Contact;
import de.unibayreuth.bayceer.bayeos.gateway.model.Domain;
import de.unibayreuth.bayceer.bayeos.gateway.model.LatLon;
import de.unibayreuth.bayceer.bayeos.gateway.model.Notification;
import de.unibayreuth.bayceer.bayeos.gateway.model.Permission;
import de.unibayreuth.bayceer.bayeos.gateway.model.Point;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.DomainRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.NotificationRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardGroupRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.ContactRepository;
import de.unibayreuth.bayceer.bayeos.gateway.service.BoardTemplateService;

@Controller
public class BoardController extends AbstractController {

	@Autowired
	DataTableSearch boardSearch;

	@Autowired
	BoardTemplateRepository repoBoardTemplate;

	@Autowired
	BoardGroupRepository repoGroup;

	@Autowired
	ContactRepository repoContacts;

	@Autowired
	BoardTemplateService serviceBoardTemplate;

	@Autowired
	BoardRepository repo;

	@Autowired
	DomainRepository repoDomain;

	@Autowired
	NotificationRepository repoNotification;

	@RequestMapping(value = { "/", "/boards" }, method = RequestMethod.GET)
	public String list(Model model) {
		model.addAttribute("boardSearch", boardSearch);
		model.addAttribute("writeable", userSession.getUser().isUser());
		return "listBoard";
	}

	@RequestMapping(value = "/boards", params = { "origin" }, method = RequestMethod.GET)
	public String editByOriginParam(@RequestParam("origin") String origin,
			@RequestParam(value = "domain", required = false) String domain,
			@RequestParam(defaultValue = "channels") String tab) {
		Board b = findBoardByOrigin(origin, domain);
		return "redirect:/boards/" + b.getId() + "?tab=" + tab;
	}

	@RequestMapping(path = "/boards/{id}", method = RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model, @RequestParam(defaultValue = "channels") String tab) {
		Board b = repo.findOne(userSession.getUser(), id);
		if (b != null && b.getChannels() != null) {
			Collections.sort(b.getChannels());
		}
		model.addAttribute("board", b);
		model.addAttribute("groups", repoGroup.findAll(userSession.getUser(), null));
		model.addAttribute("tab", tab);
		model.addAttribute("writeable", isWriteable(b));
		return "editBoard";
	}

	@RequestMapping(path = "/boards/origin/{origin:.+}", method = RequestMethod.GET)
	public String editByOrigin(@PathVariable String origin,
			@RequestParam(value = "domain", required = false) String domain,
			@RequestParam(defaultValue = "channels") String tab) {
		Board b = findBoardByOrigin(origin, domain);
		return "redirect:/boards/" + b.getId() + "?tab=" + tab;
	}

	private Board findBoardByOrigin(String origin, String domain) {
		Domain d = resolveDomain(domain);
		Board b = repo.findByOriginIgnoreCaseAndDomain(origin, d);
		if (b == null) {
			throw new EntityNotFoundException("Could not find board with origin: " + origin);
		}
		checkRead(b);
		return b;
	}

	private Domain resolveDomain(String domain) {
		if (domain == null || domain.trim().isEmpty()) {
			return userDomain();
		}

		Domain d = repoDomain.findOneByName(domain);
		if (d != null) {
			return d;
		}

		try {
			Long id = Long.valueOf(domain);
			return repoDomain.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Could not find domain: " + domain));
		} catch (NumberFormatException e) {
			throw new EntityNotFoundException("Could not find domain: " + domain);
		}
	}

	@RequestMapping(path = "/boards/delete/{id}", method = RequestMethod.GET)
	public String delete(@PathVariable Long id, RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(), id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/boards";
	}

	@RequestMapping(value = "/boards", params = { "save" }, method = RequestMethod.POST)
	public String save(@Valid Board board, BindingResult bindingResult, RedirectAttributes redirect, Locale locale) {
		if (bindingResult.hasErrors()) {
			return "editBoard";
		}
		repo.save(userSession.getUser(), board);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/boards";
	}

	@RequestMapping(value = "/boards/applyTemplate", method = RequestMethod.POST)
	public String applyTemplate(@RequestParam("templateId") Long templateId, @RequestParam("boardId") Long boardId,
			RedirectAttributes redirect, Locale locale) {
		serviceBoardTemplate.applyTemplate(boardId, templateId);
		redirect.addFlashAttribute("globalMessage",
				msg.getMessage("e.template", null, locale) + " " + msg.getMessage("action.applied", null, locale));
		return "redirect:/boards/" + boardId;
	}

	@RequestMapping(value = "/boards/selectTemplate/{id}", method = RequestMethod.GET)
	public String selectBoardTemplate(@PathVariable Long id, Model model) {
		model.addAttribute("boardTemplates", repoBoardTemplate.findAll(userSession.getUser(), null));
		model.addAttribute("boardId", id);
		return "selectBoardTemplate";
	}

	@RequestMapping(value = "/boards/saveAsTemplate/{id}", method = RequestMethod.GET)
	public String saveAsTemplate(@PathVariable Long id) {
		BoardTemplate t = serviceBoardTemplate.saveAsTemplate(id);
		return "redirect:/boardTemplates/" + t.getId();
	}

	@RequestMapping(value = "/boards/setChannelNamebyNumber/{id}", method = RequestMethod.GET)
	public String setChannelNamebyNumber(@PathVariable Long id, RedirectAttributes redirect, Locale locale) {
		Board b = repo.findOne(userSession.getUser(), id);
		for (Channel c : b.getChannels()) {
			c.setName(c.getNr());
		}
		repo.save(b);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/boards/" + id;

	}

	@RequestMapping(value = "/boards/chart/{id}")
	public String chartBoard(@PathVariable Long id, Model model) {
		Board b = repo.findOne(userSession.getUser(), id);
		if (b != null && b.getChannels() != null) {
			Collections.sort(b.getChannels());
		}
		model.addAttribute("board", b);
		return "chartBoard";
	}

	@RequestMapping(value = "/boards/selectContacts/{id}", method = RequestMethod.GET)
	public String selectContacts(@PathVariable Long id, Model model) {
		Board b = repo.findOne(userSession.getUser(), id);
		model.addAttribute("parent", b);
		model.addAttribute("controller", "boards");
		List<Contact> cs = repoContacts.findAll(userSession.getUser(), domainFilter);
		model.addAttribute("contacts", cs);
		return "selectContacts";
	}

	@RequestMapping(value = "/boards/createNewContact/{id}", method = RequestMethod.GET)
	public String createNewContact(@PathVariable Long id, Model model) {
		Board b = repo.findOne(userSession.getUser(), id);
		Contact c = new Contact();
		c.setDomain(b.getDomain());
		model.addAttribute("parent", b);
		model.addAttribute("controller", "boards");
		model.addAttribute("contact", c);
		return "createNewContact";
	}

	@RequestMapping(value = "/boards/addNewContact", method = RequestMethod.POST)
	public String addNewContact(@Valid Contact contact, Long id) {
		Board b = repo.findOne(userSession.getUser(), id);
		if (b != null) {
			Contact c = repoContacts.findOneByEmailAndDomain(contact.getEmail(), b.getDomain());
			if (c == null) {
				contact.setDomain(b.getDomain());
				c = repoContacts.save(contact);
			}

			Notification n = new Notification();
			n.setBoard(b);
			n.setContact(c);
			repoNotification.save(n);
		}
		return "redirect:/boards/" + id + "?tab=notifications";

	}

	@RequestMapping(value = "/boards/addContacts", method = RequestMethod.POST)
	public String addContacts(@RequestParam("id") Long id, @RequestParam("contacts") List<Long> contacts) {
		Board b = repo.findOne(userSession.getUser(), id);
		for (Long i : contacts) {
			Contact c = repoContacts.findOne(userSession.getUser(), i);
			if (c != null) {
				Notification n = new Notification();
				n.setBoard(b);
				n.setContact(c);
				repoNotification.save(n);
			}
		}
		return "redirect:/boards/" + b.getId() + "?tab=notifications";
	}

	@RequestMapping(value = "/boards/removeNotification/{id}", method = RequestMethod.GET)
	public String removeNotification(@PathVariable("id") Long id) {
		Notification n = repoNotification.findById(id).orElseThrow(() -> new EntityNotFoundException());
		checkWrite(n.getBoard());
		Long b = n.getBoard().getId();
		repoNotification.deleteById(n.getId());
		return "redirect:/boards/" + b + "?tab=notifications";
	}

	@RequestMapping(value = "/boards/map/{id}")
	public String showMap(@PathVariable("id") Long id, Model model, Locale locale) {
		Board b = repo.findOne(userSession.getUser(), id);
		model.addAttribute("id", id);
		model.addAttribute("name", getMsg("e.board", locale) + ' ' + b.getName());
		model.addAttribute("point", new LatLon(b.getLat(), b.getLon()));
		model.addAttribute("writeable", userSession.getUser().isUser());

		return "mapBoard";
	}

	@RequestMapping(value = "/boards/map", method = RequestMethod.POST)
	public String saveMap(@RequestParam("id") Long id, @RequestParam("lat") Float lat, @RequestParam("lon") Float lon,
			RedirectAttributes redirect, Locale locale) {
		Board b = repo.findOne(userSession.getUser(), id);
		b.setLat(lat);
		b.setLon(lon);
		repo.save(userSession.getUser(), b);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/boards/" + b.getId();
	}

}