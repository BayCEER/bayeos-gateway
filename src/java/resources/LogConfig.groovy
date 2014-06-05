
log4j = {
	appenders {
			rollingFile name:'file', file:'/var/log/tomcat6/gateway.log'		
	}
	
	root {
		error 'file'				
	}
	
	info "grails.app.services"
	info "grails.app.controllers"
	info "grails.app.utils"
	
}
		
	
