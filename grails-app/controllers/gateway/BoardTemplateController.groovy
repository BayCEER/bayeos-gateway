package gateway

import groovy.xml.MarkupBuilder
import org.springframework.dao.DataIntegrityViolationException

class BoardTemplateController {



	def index() {
		redirect action: 'list', params: params
	}

	def list() {		
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		
		[boardTemplateInstance: BoardTemplate.listOrderByName(params), total: BoardTemplate.count()]
	}
	
	def upload() {
		
	}

	def create() {
		switch (request.method) {
			case 'GET':
				[boardTemplateInstance: new BoardTemplate(params)]
				break
			case 'POST':
				def boardTemplateInstance = new BoardTemplate(params)
				if (!boardTemplateInstance.save(flush: true)) {
					render view: 'create', model: [boardTemplateInstance: boardTemplateInstance]
					return
				}

				flash.message = message(code: 'default.created.message', args: [
					message(code: 'boardTemplate.label', default: 'BoardTemplate'),
					boardTemplateInstance.id
				])
				redirect action: 'list'
				break
		}
	}

	def edit() {
		def boardTemplateInstance = BoardTemplate.get(params.id)
		if (!boardTemplateInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'boardTemplate.label', default: 'BoardTemplate'),
				params.id
			])
			flash.level = 'warning'
			redirect action: 'list'
			return
		}
		
		
		switch (request.method) {
			case 'GET':
				[boardTemplateInstance: boardTemplateInstance]
				break
			case 'POST':				
				if (params.version) {
					def version = params.version.toLong()
					if (boardTemplateInstance.version > version) {
						boardTemplateInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
								[
									message(code: 'boardTemplate.label', default: 'BoardTemplate')] as Object[],
								"Another user has updated this BoardTemplate while you were editing")
						render view: 'edit', model: [boardTemplateInstance: boardTemplateInstance]
						return
					}
				}

				boardTemplateInstance.properties = params

				if (!boardTemplateInstance.save(flush: true)) {
					render view: 'edit', model: [boardTemplateInstance: boardTemplateInstance]
					return
				}

				flash.message = message(code: 'default.updated.message', args: [
					message(code: 'boardTemplate.label', default: 'BoardTemplate'),
					boardTemplateInstance.id
				])
				redirect action: 'list'
				break
		}
	}

	def delete() {
		def boardTemplateInstance = BoardTemplate.get(params.id)
		if (!boardTemplateInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'boardTemplate.label', default: 'BoardTemplate'),
				params.id
			])
			flash.level = 'warning'
			redirect action: 'list'
			return
		}

		try {
			boardTemplateInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [
				message(code: 'boardTemplate.label', default: 'BoardTemplate'),
				params.id
			])
			redirect action: 'list'
		}
		catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [
				message(code: 'boardTemplate.label', default: 'BoardTemplate'),
				params.id
			])
			flash.level = 'danger'
			render view: 'edit', model: [boardTemplateInstance: boardTemplateInstance]
		}
	}

	def uploadFile() {

		def uploadedFile = request.getFile('payload')
		if(uploadedFile && uploadedFile.size>0){
			def board = new XmlParser().parse(uploadedFile.getInputStream())

			if (BoardTemplate.findByName(board['@name'])){
				flash.message = "Board template ${board['@name']} already exists."
				flash.level = 'warning'
				redirect(action:"upload")
				return
			}

			def template = new BoardTemplate(name:board['@name'],revision:board['@revision'],dataSheet:board['@dataSheet'], description:board['@description'])

			if (board['@samplingInterval']){
				template.samplingInterval = Integer.valueOf(board['@samplingInterval'])
			}
			if (board['@aggrInterval']){
				template.aggrInterval = Interval.findOrSaveWhere(name:board['@aggrInterval'])
			}
			if (board['@aggrFunction']){
				template.aggrFunction = Function.findOrSaveWhere(name:board['@aggrFunction'])
			}

			if (board['@criticalMax']){
				template.criticalMax = Float.valueOf(board['@criticalMax'])
			}
			if (board['@criticalMin']){
				template.criticalMin = Float.valueOf(board['@criticalMin'])
			}
			if (board['@warningMax']){
				template.warningMax = Float.valueOf(board['@warningMax'])
			}
			if (board['@warningMin']){
				template.warningMin = Float.valueOf(board['@warningMin'])
			}
			if (board['@checkDelay']){
				template.checkDelay = Integer.valueOf(board['@checkDelay'])
			}
			




			board.channel.each { ch ->

				def cht = new ChannelTemplate(nr:Integer.valueOf(ch['@nr']),label:ch['@label'],phenomena:ch['@phenomena'])
				if (ch['@aggrInterval']){
					cht.aggrInterval = Interval.findOrSaveWhere(name:ch['@aggrInterval'])
				}
				if (ch['@aggrFunction']){
					cht.aggrFunction = Function.findOrSaveWhere(name:ch['@aggrFunction'])
				}
				if (ch['@criticalMax']){
					cht.criticalMax = Float.valueOf(ch['@criticalMax'])
				}
				if (ch['@criticalMin']){
					cht.criticalMin = Float.valueOf(ch['@criticalMin'])
				}
				if (ch['@warningMax']){
					cht.warningMax = Float.valueOf(ch['@warningMax'])
				}
				if (ch['@warningMin']){
					cht.warningMin = Float.valueOf(ch['@warningMin'])
				}
				if (ch['@samplingInterval']){
					cht.samplingInterval = Integer.valueOf(ch['@samplingInterval'])
				}
				
				if (ch['@checkDelay']){
					cht.checkDelay = Integer.valueOf(ch['@checkDelay'])
				}
				

				if (ch.unit) {
					String u = ch.unit[0]['@name']
					cht.setUnit(Unit.findOrSaveWhere(name:u))
				}
				if (ch.spline) {
					String s = ch.spline[0]['@name']
					Spline spline = Spline.findByName(s)
					if (!spline){
						spline = new Spline(name:s)
						ch.spline[0].knotePoint.each{ kp ->
							spline.addToKnotePoints(new KnotPoint(x:Float.valueOf(kp['@x']),y:Float.valueOf(kp['@y'])))
						}
						spline.save()
					}
					cht.setSpline(spline)
				}
				template.addToChannelTemplates(cht)
			}


			if(!template.hasErrors() && template.save()) {
				flash.message = "Board template ${template.id} created"
				redirect(action:"list")
			} else {
				flash.message = "Failed to create board template."
				flash.level = 'danger'
				redirect(action:"list")
			}
		} else {
			flash.message = "Failed to upload board template."
			flash.level = 'danger'
			redirect(action:"list")
		}
	}

	def export() {
		def board = BoardTemplate.get(params.id)
		StringWriter writer = new StringWriter()
		MarkupBuilder xml = new MarkupBuilder(writer)
		xml.setOmitNullAttributes(true)
		xml.setOmitEmptyAttributes(true)


		xml.board(name:board.name,description:board.description,revision:board.revision,
		dataSheet:board.dataSheet, criticalMax:board.criticalMax,criticalMin:board.criticalMin,
		warningMax:board.warningMax,warningMin:board.warningMin,
		samplingInterval:board.samplingInterval,checkDelay:board.checkDelay){

			board.channelTemplates.each { item ->
				channel(nr:item.nr, label:item.label, phenomena:item.phenomena,aggrInterval:item.aggrInterval, aggrFunction:item.aggrFunction,
				criticalMax:item.criticalMax,criticalMin:item.criticalMin,warningMax:item.warningMax,warningMin:item.warningMin,samplingInterval:item.samplingInterval,checkDelay:item.checkDelay){
					if (item.unit){
						unit(name:item.unit.name)
					}

					if (item.spline){
						spline(name:item.spline?.name){
							item.spline?.knotePoints?.each { kp ->
								knotePoint(x:kp.x,y:kp.y)
							}
						}
					}
				}
			}
		}
		response.setHeader("Content-disposition", "attachment;filename=${board.name.encodeAsURL()}.xml")
		render(contentType:"text/xml",text:writer.toString())
	}
}
