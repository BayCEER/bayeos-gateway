package gateway

class DeleteJobController {

	def deleteObservationService

	def edit() {

		def config = DeleteJobConfig.first()

		switch (request.method) {
			case 'GET':
				[name:"Delete Job", config:config, started:deleteObservationService.started()]				
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
					if (!deleteObservationService.restart()) {
						flash.message = "Failed to restart service."
						flash.level = "danger"
					}

				} else {
					if (!deleteObservationService.stop()) {
						flash.message = "Failed to stop service."
						flash.level = "danger"
					}
				}

				break

		}

		[name:"Delete Job", config:config, started:deleteObservationService.started()]


	}

	def stop() {
		if (!deleteObservationService.stop()) {
			flash.message = "Failed to stop service."
			flash.level = "danger"
		}
		redirect(action: "edit")
	}

	def start() {
		if (!deleteObservationService.start()) {
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
