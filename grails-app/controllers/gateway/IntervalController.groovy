package gateway

import org.springframework.dao.DataIntegrityViolationException

class IntervalController {



    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [intervalInstanceList: Interval.list(params), intervalInstanceTotal: Interval.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[intervalInstance: new Interval(params)]
			break
		case 'POST':
	        def intervalInstance = new Interval(params)
	        if (!intervalInstance.save(flush: true)) {
	            render view: 'create', model: [intervalInstance: intervalInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'interval.label', default: 'Interval'), intervalInstance.id])
	        redirect action: 'list'
			break
		}
    }
    
    def edit() {
		switch (request.method) {
		case 'GET':
	        def intervalInstance = Interval.get(params.id)
	        if (!intervalInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'interval.label', default: 'Interval'), params.id])
				flash.level = 'danger'
	            redirect action: 'list'
	            return
	        }

	        [intervalInstance: intervalInstance]
			break
		case 'POST':
	        def intervalInstance = Interval.get(params.id)
	        if (!intervalInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'interval.label', default: 'Interval'), params.id])
				flash.level = 'danger'
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (intervalInstance.version > version) {
	                intervalInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'interval.label', default: 'Interval')] as Object[],
	                          "Another user has updated this Interval while you were editing")
	                render view: 'edit', model: [intervalInstance: intervalInstance]
	                return
	            }
	        }

	        intervalInstance.properties = params

	        if (!intervalInstance.save(flush: true)) {
	            render view: 'edit', model: [intervalInstance: intervalInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'interval.label', default: 'Interval'), intervalInstance.id])
	        redirect action: 'list'
			break
		}
    }

    def delete() {
        def intervalInstance = Interval.get(params.id)
        if (!intervalInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'interval.label', default: 'Interval'), params.id])
			flash.level = 'danger'
            redirect action: 'list'
            return
        }

        try {
            intervalInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'interval.label', default: 'Interval'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'interval.label', default: 'Interval'), params.id])
			flash.level = 'danger'
            render action: 'list'
        }
    }
}
