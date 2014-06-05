class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}				
				
		"/nagios/gateway/"(controller: "nagios") {
			action = [GET: "msgGateway"]
		}
				
		"/nagios/group/$name"(controller: "nagios") {
			action = [GET: "msgGroup"]
		}
						
		"/nagios/board/$id"(controller: "nagios") {
			action = [GET: "msgBoard"]
		}
		
		"/nagios/channel/$id"(controller: "nagios") {
			action = [GET: "msgChannel"]
		}
		
		"/nagios/exporter/"(controller: "nagios") {
			action = [GET: "msgExporter"]
		}
		
		
		"/"(controller:"board",action = "list")
			 				
		
		"500"(view:'/error')
	}
}
