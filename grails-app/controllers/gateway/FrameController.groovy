package gateway

import javax.tools.ForwardingFileObject;


class FrameController {

	def frameService
	
	def index() {
		
	}
	
	
	def saveFlat() {				
		String sender = "IP:" + request.getRemoteAddr()				
		if (params.containsKey("sender")){			
			sender = params.get("sender")			
		}	
		log.info("Received a package from ${sender}")
		def ret = false
		if (params.containsKey("bayeosframe")){
			ret = frameService.saveFlatFrames(sender,new Date(),[params.bayeosframe])
		} else {
			ret = frameService.saveFlatFrames(sender,new Date(),params.get("bayeosframes[]"))
		}
		if (ret){
			logAndRender(200,'Frames saved.')
		} else {
			logAndRender(500,'Failed to save frames.')
		}	
	
	}

	def saveMatrix() {				
		String sender = "IP:" + request.getRemoteAddr()
		if (params.containsKey("sender")){
			sender = params.get("sender")
		}
		log.info("Received a package from ${sender}")
		def ret = false
		if (params.containsKey("bayeosframe")){
			ret = frameService.saveMatrixFrames(sender,new Date(),[params.bayeosframe])
		} else {
			ret = frameService.saveMatrixFrames(sender,new Date(),params.get("bayeosframes[]"))
		}
		if (ret){
			logAndRender(200,'Frames saved.')
		} else {
			logAndRender(500,'Failed to save frames.')
		}
	
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
