package gateway

import grails.converters.JSON
import grails.converters.XML
import groovy.sql.Sql

class ChannelController {

	def dataSource
	def observationService
	def nagiosService
	def channelService
	
	
	
	def channelData() {
		render(contentType:"text/json") {
			observations  = array {
				for(o in observationService.findByChannelAndTime(myId,nr,rowId)) {
					ob my:myId, ch:o.channel_nr, millis:o.res
				}
			}			
		}
		
	}
	
	
	def edit() {
		def channelInstance = Channel.get(params.id)
		if (!channelInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'channel.label', default: 'Channel'), params.id])
			flash.level = 'danger'			
			return
		}
		
		switch (request.method) {
		case 'GET':			
			[channelInstance: channelInstance]
			break
		case 'POST':		
			if (params.version) {
				def version = params.version.toLong()
				if (channelInstance.version > version) {
					channelInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
							  [message(code: 'channel.label', default: 'Channel')] as Object[],
							  "Another user has updated this Channel while you were editing")
					render view: 'edit', model: [channelInstance: channelInstance]
					return
				}
			}

			channelInstance.properties = params

			if (!channelInstance.save(flush: true)) {
				render view: 'edit', model: [channelInstance: channelInstance]
				return
			}

			flash.message = message(code: 'default.updated.message', args: [message(code: 'channel.label', default: 'Channel'), channelInstance.id])
			redirect controller:"board", action: "edit" , id: channelInstance.board.id
			break
		}
	}
	
	
	def delete() {
		def channelInstance = Channel.get(params.id)
		if (channelInstance) {
			try {
				channelInstance.delete(flush: true)
				flash.message = "Channel ${channelInstance.id} deleted."
			} catch (Exception e){
				flash.message = "Failed to delete channel."
				flash.level = "danger"				
			}
			redirect controller:"board", action:"edit", id:channelInstance.board.id
			
		} else {	
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'channel.label', default: 'Channel'), params.id])}"
			flash.level = "warning"			
		}
		
	}
	
	def toggleNagios() {
		log.info(params)
		Channel c = Channel.get(params.id)
				
		if (c){
			c.excludeFromNagios = (params.enabled == 'false')			
			c.save()
		}
		render ''
	}
	
   
}
