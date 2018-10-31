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

import de.unibayreuth.bayceer.bayeos.gateway.marshaller.SplineMarshaller;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;
import de.unibayreuth.bayceer.bayeos.gateway.repo.SplineRepository;

@Controller
public class SplineController extends AbstractController{
	
	private final static Logger log = Logger.getLogger(SplineController.class);
	
	@Autowired
	SplineRepository repo;
					
	@RequestMapping(value="/splines/save", method=RequestMethod.POST)
	public String save(@Valid Spline spline, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
		if (bindingResult.hasErrors()){
			return "editSpline";
		}				
		repo.save(userSession.getUser(),spline);
		redirect.addFlashAttribute("globalMessage", getActionMsg("saved", locale));
		return "redirect:/splines";
	}
	
	
	@RequestMapping(value="/splines", method=RequestMethod.GET)
	public String list(Model model, @SortDefault("name") Pageable pageable){
		model.addAttribute("splines", repo.findAll(userSession.getUser(),domainFilter,pageable));
		return "listSpline";
	}
	
	@RequestMapping(value="/splines/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Long id, Model model){	
		Spline s = repo.findOne(userSession.getUser(),id);
		model.addAttribute("spline",s);
		model.addAttribute("writeable",isWriteable(s));
		return "editSpline";		
	}
	
	@RequestMapping(value="/splines/delete/{id}", method=RequestMethod.GET)
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		repo.delete(userSession.getUser(),id);
		redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));
		return "redirect:/splines";
	}
	
	@RequestMapping(value="/splines/upload", method=RequestMethod.GET)
	public String upload(){
		return "uploadSpline";
	}
	
	@RequestMapping(value="/splines/upload", method=RequestMethod.POST)
	public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
			try {
				Spline s = SplineMarshaller.unmarshal(file.getInputStream());
				s.setDomain(userSession.getUser().getDomain());
				repo.save(s);				
			} catch (IOException e) {
				log.error(e.getMessage());
				redirect.addFlashAttribute("globalMessage", "Failed to save spline.");
				return "uploadSpline";			
			}						
			redirect.addFlashAttribute("globalMessage", "Spline saved.");									
			return "redirect:/splines";
	}
	
	@RequestMapping(value="/splines/export/{id}", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> export(@PathVariable Long id, RedirectAttributes redirect) throws UnsupportedEncodingException, IOException {														
			Spline s =  repo.findOne(userSession.getUser(),id);			
			if (s == null) throw new IOException("Failed to read spline.");
			String b = SplineMarshaller.marshal(s);			
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+ URLEncoder.encode(s.getName(),"UTF-8") +".xml").body(b.toString());
		
	}
	
	
		
			
	
}
