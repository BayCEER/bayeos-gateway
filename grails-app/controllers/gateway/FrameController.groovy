package gateway

import javax.tools.ForwardingFileObject;


class FrameController {

	def frameService
	
	def index() {
		
	}
	
	/*
	 *  @since 1.9.21
	 */
	def save() {
		String sender = "IP:" + request.getRemoteAddr()
		if (params.containsKey("sender")){
			sender = params.get("sender")
		}
		log.info("Received a package from ${sender}")
		def ret = false
		if (params.containsKey("bayeosframe")){
			ret = frameService.saveFlatFrames(sender,[params.bayeosframe])
		} else {
			ret = frameService.saveFlatFrames(sender,params.get("bayeosframes[]"))
		}
		if (ret){
			logAndRender(200,'Frames saved.')
		} else {
			logAndRender(500,'Failed to save frames.')
		}
	}
			
	/*
	 *@deprecated 
	 */
	def saveFlat() {					
		redirect(action: "save", params: params)
	}

	/*
	 * @deprecated 
	 */
	def saveMatrix() {				
		redirect(action: "save", params: params)
	}



	private logAndRender(status, text) {
		if (status == 200) {
			log.debug(text)
		} else if (status> 200 && status < 500) {
			log.warn(text)
		} else {
			log.error(text)
		}
		render(status: status, text: text)
	}
}
