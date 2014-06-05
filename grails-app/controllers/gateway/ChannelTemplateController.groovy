package gateway

import org.springframework.dao.DataIntegrityViolationException

class ChannelTemplateController {



     
    def create() {					
		ChannelTemplate chTemplate = new ChannelTemplate(params)
		BoardTemplate t = BoardTemplate.get(params.boardTemplateId)		
		chTemplate.boardTemplate = t
		chTemplate.nr = t.channelTemplates.size() + 1
		
		switch (request.method) {
		case 'GET':
			[channelTemplateInstance: chTemplate]
			break
		case 'POST':			    
	        if (!chTemplate.save(flush: true)) {
	            render view: 'create', model: [channelTemplateInstance: chTemplate]
	            return
	        }
			flash.message = message(code: 'default.created.message', args: [message(code: 'channelTemplate.label', default: 'ChannelTemplate'), chTemplate.id])
			redirect controller:'boardTemplate', action: 'edit', id: chTemplate.boardTemplate.id 
			break
		}
    }
    
    def edit() {
		def channelTemplateInstance = ChannelTemplate.get(params.id)
		if (!channelTemplateInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'channelTemplate.label', default: 'ChannelTemplate'), params.id])
			flash.level = 'warning'
			return
		}
		
		switch (request.method) {
		case 'GET':	        
	        [channelTemplateInstance: channelTemplateInstance]
			break
		case 'POST':	        
	        if (params.version) {
	            def version = params.version.toLong()
	            if (channelTemplateInstance.version > version) {
	                channelTemplateInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'channelTemplate.label', default: 'ChannelTemplate')] as Object[],
	                          "Another user has updated this ChannelTemplate while you were editing")
	                render view: 'edit', model: [channelTemplateInstance: channelTemplateInstance]
	                return
	            }
	        }

	        channelTemplateInstance.properties = params

	        if (!channelTemplateInstance.save(flush: true)) {
	            render view: 'edit', model: [channelTemplateInstance: channelTemplateInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'channelTemplate.label', default: 'ChannelTemplate'), channelTemplateInstance.id])
	        redirect controller:'boardTemplate', action: 'edit', id: channelTemplateInstance.boardTemplate.id
			break
		}
    }

    def delete() {
        def channelTemplateInstance = ChannelTemplate.get(params.id)
        if (channelTemplateInstance) {
			try {
				channelTemplateInstance.delete(flush: true)
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'channelTemplate.label', default: 'ChannelTemplate'), params.id])
				
			} catch (Exception e) {
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'channelTemplate.label', default: 'ChannelTemplate'), params.id])
				flash.level = 'danger'
			}
			redirect controller:'boardTemplate', action: 'edit', id: channelTemplateInstance.boardTemplate.id			                    
        } else {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'channelTemplate.label', default: 'ChannelTemplate'), params.id])
			flash.level = 'warning'
        }
    }
}
