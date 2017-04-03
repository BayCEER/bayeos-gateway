package de.unibayreuth.bayceer.bayeos.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrameEventController {
	
	@RequestMapping(path="/frameEvents")
	public String list(){
		return "listFrameEvents";
	}
	
}
