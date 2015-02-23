package gateway

class ExportJobController {

	def exportObservationService
	def nagiosService

	def edit() {

		ExportJobConfig config = ExportJobConfig.first()				
		switch (request.method) {
			case 'GET':						
				[name:"Export Job", config:config, started:exportObservationService.started(), nagiosMsg:nagiosService.msgExporter()]				
				break
			case 'POST':				
				config.properties = params																	
				if (!config.save(flush:true)) {
					flash.message = "Failed to save settings."					
					flash.level = "danger"										
				} else {
					flash.message = "Settings saved"
					flash.level = "info"
				}

				if (config.enabled) {
					if (!exportObservationService.restart()) {
						flash.message = "Failed to restart service."
						flash.level = "danger"
					}

				} else {
					if (exportObservationService.started()) {
						if (!exportObservationService.stop()) {
							flash.message = "Failed to stop service."
							flash.level = "danger"
						}
					}					
				}
			
				[name:"Export Job", config:config, started:exportObservationService.started(),nagiosMsg:nagiosService.msgExporter()]
				break
		}

		


	}

	def stop() {
		if (!exportObservationService.stop()) {
			flash.message = "Failed to stop service."
			flash.level = "danger"
		}
		redirect(action: "edit")
	}

	def start() {
		if (!exportObservationService.start()) {
			flash.message = "Failed to start service."
			flash.level = "danger"
		}
		redirect(action: "edit")
	}


	def toggle() {
		log.debug("toggle")
		if (params.enabled == 'false') {
			stop()
		} else {
			start()
		}

	}




}
