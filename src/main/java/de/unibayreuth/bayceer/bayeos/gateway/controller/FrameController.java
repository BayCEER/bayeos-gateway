package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import bayeos.frame.types.Command;
import de.unibayreuth.bayceer.bayeos.gateway.model.BoardCommand;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.BoardCommandRepository;
import de.unibayreuth.bayceer.bayeos.gateway.service.FrameService;

@Controller
public class FrameController extends AbstractController {
	
	@Autowired
	FrameService frameService;
	
	@Autowired 
	BoardCommandRepository repoCommand;
	
	private Logger log = LoggerFactory.getLogger(FrameController.class);

	@RequestMapping(path = "/frame/saveFlat", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public ResponseEntity saveFlat(@RequestParam MultiValueMap<String,String> params, HttpServletRequest request) {						
			
			try {				
				String sender = getSender(params);
				List<String> frames = getFrames(params);
				log.info("Received " + frames.size() + " frames from " + sender);															
				if (frameService.saveFrames(userSession.getUser().getDomainId(), sender,frames)) {
					log.debug("Frames saved.");					
					HttpHeaders responseHeaders = new HttpHeaders();
					responseHeaders.set("Content-Type", "application/x-www-form-urlencoded");
					return new ResponseEntity<String>(getCallback(sender), responseHeaders, HttpStatus.OK);
															
				} else {
					log.warn("Failed to save frames.");
					return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
				}
								
			} catch (RuntimeException e){
				log.error(e.getMessage());
				log.warn("Failed to save frames.");
				return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
			} 
			
	}
	
	private String getSender(MultiValueMap<String,String> params){
		String sender = "unknown";			
		if (params.containsKey("sender")){
			sender = params.get("sender").get(0);
		} 
		return sender;
	}
	
	private String getCallback(String sender) {							
		StringBuffer bf = new StringBuffer();		
		BoardCommand cmd = repoCommand.findFirstPendingByOrigin(sender);		
		if (cmd !=null) {
			bf.append("bayeosframe=");				
			Command fc = new Command(cmd.getKind().byteValue(),cmd.getValue());																		
			bf.append(Base64.getEncoder().encodeToString(fc.getBytes()));
		}
							
   	    try {
			return URLEncoder.encode(bf.toString(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
			return "";
		} 					
	}
	
	private List<String> getFrames(MultiValueMap<String,String> params){
		List<String> frames = new ArrayList<String>();			
		frames.addAll(params.get("bayeosframes[]"));						
		if (params.containsKey("bayeosframe")){
			frames.addAll(params.get("bayeosframe"));	
		}
		return frames;
	}

	@RequestMapping(value = "/postFrame", method = RequestMethod.GET)
    public String postFrame(Model model) {    	
        return "postFrame";
    }


}
