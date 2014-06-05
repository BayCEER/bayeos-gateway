package gateway

import grails.converters.JSON
import grails.converters.XML

class NagiosController {
	
	def nagiosService
	
		
	// Example call: curl -v -i -u nagios:xbee localhost:8080/bayeos-gateway/nagios/msgGateway
	def msgGateway(){
		def msg = nagiosService.msgGateway();
		withFormat {
			xml { render msg as XML}
			json { render msg as JSON}
		}
	}
	
	def msgExporter(){
		def msg = nagiosService.msgExporter();
		withFormat {
			xml { render msg as XML}
			json { render msg as JSON}
		}
	}

	def msgGroup(){
		def msg = nagiosService.msgGroup(params.name)
		withFormat {
			xml { render msg as XML}
			json { render msg as JSON}
		}
	}
	
	
    def msgBoard() { 
		def msg = nagiosService.msgBoard(params.int('id'))
		withFormat {
			xml { render msg as XML}
			json { render msg as JSON}
		}
	}
	
	def msgChannel(){
		def msg = nagiosService.msgChannel(params.int('id'))
		withFormat {
			xml { render msg as XML}
			json { render msg as JSON}
		}
	}
	
	
	
	
	
	
	
	
	
}
