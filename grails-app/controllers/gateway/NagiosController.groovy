package gateway

import grails.converters.XML
import grails.converters.JSON

class NagiosController {
	
	def nagiosService
				
	// Example call: curl -v -i -u nagios:xbee localhost:8080/bayeos-gateway/nagios/msgGateway
	def msgGateway(){							
		def msg = nagiosService.msgGateway()
		withFormat {
			xml{render(text:msg as XML,contentType:"text/xml",encoding:"UTF-8")}
			json{render(text:msg as JSON,contentType:"text/json",encoding:"UTF-8")}			
		}
				
	}
	
	def msgExporter(){
		def msg = nagiosService.msgExporter();
		withFormat {
			xml{render(text:msg as XML,contentType:"text/xml",encoding:"UTF-8")}
			json{render(text:msg as JSON,contentType:"text/json",encoding:"UTF-8")}
		}
		
	}

	def msgGroup(){
		def msg = nagiosService.msgGroup(params.name)
		withFormat {
			xml{render(text:msg as XML,contentType:"text/xml",encoding:"UTF-8")}
			json{render(text:msg as JSON,contentType:"text/json",encoding:"UTF-8")}
		}
	}
	
	
    def msgBoard() { 
		def msg = nagiosService.msgBoard(params.int('id'))
		withFormat {
			xml{render(text:msg as XML,contentType:"text/xml",encoding:"UTF-8")}
			json{render(text:msg as JSON,contentType:"text/json",encoding:"UTF-8")}
		}
	}
	
	def msgChannel(){
		def msg = nagiosService.msgChannel(params.int('id'))
		withFormat {
			xml{render(text:msg as XML,contentType:"text/xml",encoding:"UTF-8")}
			json{render(text:msg as JSON,contentType:"text/json",encoding:"UTF-8")}
		}
	}
	
	
	
	
	
	
	
	
	
}
