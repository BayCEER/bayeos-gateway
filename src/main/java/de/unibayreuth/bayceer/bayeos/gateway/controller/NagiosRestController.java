package de.unibayreuth.bayceer.bayeos.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.bayeos.gateway.model.NagiosMessage;
import de.unibayreuth.bayceer.bayeos.gateway.service.NagiosService;

@RestController
public class NagiosRestController {
	
	@Autowired
	NagiosService service;
	
			
	@RequestMapping(path="/nagios/gateway", method=RequestMethod.GET)	
	public NagiosMessage msgGateway(){		
		return service.msgGateway();
	}
	
	@RequestMapping(path="/nagios/domain/{id}", method=RequestMethod.GET)	
	public NagiosMessage msgDomain(@PathVariable Integer id){		
		return service.msgDomain(id);
	}
	
	@RequestMapping(path="/nagios/group/{id}", method=RequestMethod.GET)	
	public NagiosMessage msgGroup(@PathVariable Integer id){		
		return service.msgGroup(id);
	}
	
	@RequestMapping(path="/nagios/board/{id}", method=RequestMethod.GET)	
	public NagiosMessage msgBoard(@PathVariable Integer id){		
		return service.msgBoard(id);
	}

	@RequestMapping(path="/nagios/channel/{id}", method=RequestMethod.GET)	
	public NagiosMessage msgChannel(@PathVariable Integer id){		
		return service.msgChannel(id);
	}
	
	@RequestMapping(path="/nagios/exporter", method=RequestMethod.GET)	
	public NagiosMessage msgExporter(){		
		return service.msgExporter();
	}
	

	
	
	

	

}
