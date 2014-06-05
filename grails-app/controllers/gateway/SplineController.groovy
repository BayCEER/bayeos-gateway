package gateway

import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import org.springframework.dao.DataIntegrityViolationException

class SplineController {

	def splineService
	
    def index() {
        redirect action: 'list', params: params
    }

    def list() {
		def max = Math.min(params.max?.toInteger()?:10,100)
		def offset = params.offset?.toInteger()?:0		
        [splineInstanceList:splineService.getSplinesWithLockedFlag(max,offset),splineInstanceTotal: Spline.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[splineInstance: new Spline(params)]
			break
		case 'POST':
	        def splineInstance = new Spline(params)
	        if (!splineInstance.save(flush: true)) {
	            render view: 'create', model: [splineInstance: splineInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'spline.label', default: 'Spline'), splineInstance.id])
	        redirect action: 'list'
			break
		}
    }
    
    def edit() {
		switch (request.method) {
		case 'GET':
	        def splineInstance = Spline.get(params.id)
	        if (!splineInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'spline.label', default: 'Spline'), params.id])
				flash.level = 'danger'
	            redirect action: 'list'
	            return
	        }

	        [splineInstance: splineInstance]
			break
		case 'POST':
	        def splineInstance = Spline.get(params.id)
	        if (!splineInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'spline.label', default: 'Spline'), params.id])
				flash.level = 'danger'
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (splineInstance.version > version) {
	                splineInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'spline.label', default: 'Spline')] as Object[],
	                          "Another user has updated this Spline while you were editing")
	                render view: 'edit', model: [splineInstance: splineInstance]
	                return
	            }
	        }

	        splineInstance.properties = params

	        if (!splineInstance.save(flush: true)) {
	            render view: 'edit', model: [splineInstance: splineInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'spline.label', default: 'Spline'), splineInstance.id])
	        redirect action: 'list'
			break
		}
    }
	
	
	def upload() {
		
	}
		

    def delete() {
        def splineInstance = Spline.get(params.id)
        if (!splineInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'spline.label', default: 'Spline'), params.id])
			flash.level = 'danger'
            redirect action: 'list'
            return
        }

        try {
            splineInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'spline.label', default: 'Spline'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'spline.label', default: 'Spline'), params.id])
			flash.level = 'danger'
            render action: 'list'
        }
    }
	
	def uploadFile() {				
		 def spline = new Spline()	
		 def uploadedFile = request.getFile('payload')
		 if(uploadedFile && uploadedFile.size>0){
			 def result = new XmlParser().parse(uploadedFile.getInputStream())
			 spline.name = result.@name
			 result.data.each {
				 KnotPoint p = new KnotPoint(x:it.attribute("x"),y:it.attribute("y"))
				 spline.addToKnotePoints(p)
			 }
		 } else {
		 	flash.message = "Failed to upload spline."
			flash.level = 'danger'
		 }
		 
		 if(!spline.hasErrors() && spline.save()) {
			 flash.message = "Spline ${spline.id} created"			
		 } else {
			 flash.message = "Failed to create spline."
			 flash.level = 'danger'			 					 
		 }
		 redirect(action:"list")
		
	}
	
	def export() {
		
		def spline = Spline.get(params.id)
		StringWriter writer = new StringWriter();
		MarkupBuilder xml = new MarkupBuilder(writer);
		xml.spline(name:spline.name){
			spline.knotePoints.each { item ->
				data(x:item.x,y:item.y)
			}
		}
		response.setHeader("Content-disposition", "attachment;filename=${spline.name.encodeAsURL()}.xml");
		render(contentType:"text/xml",text:writer.toString())
	}
	
}
