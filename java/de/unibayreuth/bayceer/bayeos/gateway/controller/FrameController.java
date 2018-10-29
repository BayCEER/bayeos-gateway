package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibayreuth.bayceer.bayeos.gateway.service.FrameService;

@Controller
public class FrameController extends AbstractController {
	
	@Autowired
	FrameService frameService;
	
//	@Autowired
//	FrameImporterService frameImporter;
	
	
		
	private Logger log = Logger.getLogger(FrameController.class);

	@RequestMapping(path = "/frame/saveFlat", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public ResponseEntity saveFlat(@RequestParam MultiValueMap<String,String> params, HttpServletRequest request) {						
			
			try {				
				String sender = getSender(params);
				List<String> frames = getFrames(params);
				log.info("Received " + frames.size() + " frames from " + sender);															
				if (frameService.saveFrames(userSession.getUser().getDomainId(), sender,frames)) {
					log.debug("Frames saved.");	
					return new ResponseEntity(HttpStatus.OK);
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
	
	private List<String> getFrames(MultiValueMap<String,String> params){
		List<String> frames = new ArrayList<String>();			
		frames.addAll(params.get("bayeosframes[]"));						
		if (params.containsKey("bayeosframe")){
			frames.addAll(params.get("bayeosframe"));	
		}
		return frames;
	}


//	@RequestMapping(path = "/frame", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
//	public ResponseEntity save(@RequestParam MultiValueMap<String,String> params, HttpServletRequest request){						
//			String sender = getSender(params);
//			List<String> frames = getFrames(params);
//			log.info("Received " + frames.size() + " frames from " + sender);			
//			if (frameImporter.save(sender, frames)){
//				return new ResponseEntity(HttpStatus.OK);	
//			} else {
//				log.warn("Failed to save frames.");
//				return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);	
//			}
//	}
	

	
	


}
