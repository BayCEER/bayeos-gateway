package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.marshaller.BoardTemplateMarshaller;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardTemplateRepository;
import de.unibayreuth.bayceer.bayeos.gateway.service.BoardTemplateService;

@Controller
public class BoardTemplateController extends AbstractCRUDController {
	
	private final static Logger log = Logger.getLogger(BoardTemplateController.class);
	
	@Autowired
	BoardTemplateRepository repo;
	
	@Autowired
	BoardTemplateService boardService;
					
		
	@RequestMapping(value="/boardTemplate/save", method=RequestMethod.POST)
	public String save(@Valid BoardTemplate template, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editBoardTemplate";
		}				
		repo.save(template);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/boardTemplates";
	}
	
	
	@RequestMapping(value="/boardTemplates", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){
		model.addAttribute("boardTemplates", repo.findAll(pageable));
		return "listBoardTemplates";
	}
	
	@RequestMapping(value="/boardTemplates/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){	
		BoardTemplate s = repo.findOne(id);
		if (s!=null && s.getTemplates()!=null){
			s.getTemplates().sort(null);
		}
		model.addAttribute("boardTemplate",s);
		return "editBoardTemplate";		
	}
	
	@RequestMapping(value="/boardTemplates/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/boardTemplates";
	}
	
	@RequestMapping(value="/boardTemplate/upload", method=RequestMethod.GET)
	public String upload(){
		return "uploadBoardTemplate";
	}
	
	@RequestMapping(value="/boardTemplate/upload", method=RequestMethod.POST)
	public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
			try {
				
				BoardTemplate t = BoardTemplateMarshaller.unmarshal(file.getInputStream());
				boardService.save(t);
				
			} catch (IOException e) {
				log.error(e.getMessage());
				redirect.addFlashAttribute("globalMessage", "Failed to save board template.");
				return "uploadBoardTemplate";			
			}						
			redirect.addFlashAttribute("globalMessage", "Board template saved.");									
			return "redirect:/boardTemplates";
	}
	
	@RequestMapping(value="/boardTemplates/export/{id}", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> export(@PathVariable Long id, RedirectAttributes redirect) throws UnsupportedEncodingException, IOException {														
			BoardTemplate t =  repo.findOne(id);								
			if (t == null) throw new IOException("Failed to read board template.");			
			String b = BoardTemplateMarshaller.marshal(t);			
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+ URLEncoder.encode(t.getName(),"UTF-8") +".xml").body(b.toString());
		
	}
	
	
		
			
	
}
