package de.unibayreuth.bayceer.bayeos.gateway.controller;

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
public class FrameController {
	
	@Autowired
	FrameService frameService;
	
	private Logger log = Logger.getLogger(FrameController.class);

	@RequestMapping(path = "/frame/saveFlat", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public ResponseEntity saveFlat(@RequestParam MultiValueMap<String,String> params, HttpServletRequest request) {			
			String sender = "IP:" + request.getRemoteAddr();
			
			log.info("Received a message from " + sender);
			if (params.containsKey("sender")){
				sender = params.get("sender").get(0);
			}
			
			List<String> frames = params.get("bayeosframes[]");
			if (params.containsKey("bayeosframe")){
				frames.addAll(params.get("bayeosframe"));	
			}
			
			
			try {
				frameService.saveFrames(sender,frames);
				log.debug("Frames saved.");
				return new ResponseEntity(HttpStatus.OK);
			} catch (RuntimeException e){
				log.error(e.getMessage());
				log.warn("Failed to save frames.");
				return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
			} 
			
	}

	
	


}
