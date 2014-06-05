package gateway

import org.springframework.dao.DataIntegrityViolationException

class UnitController {



    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [unitInstanceList: Unit.list(params), unitInstanceTotal: Unit.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[unitInstance: new Unit(params)]
			break
		case 'POST':
	        def unitInstance = new Unit(params)
	        if (!unitInstance.save(flush: true)) {
	            render view: 'create', model: [unitInstance: unitInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'unit.label', default: 'Unit'), unitInstance.id])
	        redirect action: 'list'
			break
		}
    }
    
    def edit() {
		
		def unitInstance = Unit.get(params.id)
		if (!unitInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'unit.label', default: 'Unit'), params.id])
			flash.level = 'warning'
			redirect action: 'list'
			return
		}

		
		switch (request.method) {
		case 'GET':	        
	        [unitInstance: unitInstance]
			break
		case 'POST':	        
	        if (params.version) {
	            def version = params.version.toLong()
	            if (unitInstance.version > version) {
	                unitInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'unit.label', default: 'Unit')] as Object[],
	                          "Another user has updated this Unit while you were editing")
	                render view: 'edit', model: [unitInstance: unitInstance]
	                return
	            }
	        }

	        unitInstance.properties = params

	        if (!unitInstance.save(flush: true)) {
	            render view: 'edit', model: [unitInstance: unitInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'unit.label', default: 'Unit'), unitInstance.id])
	        redirect action: 'list'
			break
		}
    }

    def delete() {
        def unitInstance = Unit.get(params.id)
        if (!unitInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'unit.label', default: 'Unit'), params.id])
			flash.level = 'warning'
            redirect action: 'list'
            return
        }

        try {
            unitInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'unit.label', default: 'Unit'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'unit.label', default: 'Unit'), params.id])
			flash.level = 'danger'
			redirect action: 'list'
        }
    }
}
