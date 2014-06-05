package gateway

import groovy.sql.Sql

class BoardGroupController {
	
	def dataSource

   
    def index = {
        redirect(action: "list", params: params)
    }

	
	def list() {
		def db = new Sql(dataSource)
		def max = Math.min(params.max?.toInteger()?:10,100)
		def offset = params.offset?.toInteger()?:0
		def result = db.rows("SELECT * FROM group_status order by name LIMIT ? OFFSET ? ",[max, offset])
		[result: result, total: BoardGroup.count()]
	}


	def create() {
		switch (request.method) {
		case 'GET':
			[boardGroupInstance: new BoardGroup(params)]
			break
		case 'POST':
			def boardGroupInstance = new BoardGroup(params)
			if (!boardGroupInstance.save(flush: true)) {
				render view: 'create', model: [boardGroupInstance: boardGroupInstance]
				return
			}
			flash.message = message(code: 'default.created.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), boardGroupInstance.id])
			redirect action: 'list'
			break
		}
	}
	
	def edit() {
		def boardGroupInstance = BoardGroup.get(params.id)
		if (!boardGroupInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), params.id])
			flash.level = 'warning'
			redirect action: 'list'
			return
		}

		
		switch (request.method) {
		case 'GET':		
			[boardGroupInstance: boardGroupInstance]
			break
		case 'POST':			
			if (params.version) {
				def version = params.version.toLong()
				if (boardGroupInstance.version > version) {
					boardGroupInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
							  [message(code: 'boardGroup.label', default: 'BoardGroup')] as Object[],
							  "Another user has updated this BoardGroup while you were editing")
					render view: 'edit', model: [boardGroupInstance: boardGroupInstance]
					return
				}
			}

			boardGroupInstance.properties = params

			if (!boardGroupInstance.save(flush: true)) {
				render view: 'edit', model: [boardGroupInstance: boardGroupInstance]
				return
			}

			flash.message = message(code: 'default.updated.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), boardGroupInstance.id])
			redirect action: 'list'
			break
		}
	}

    
    
  
    def delete() {
        def boardGroupInstance = BoardGroup.get(params.id)
        if (boardGroupInstance) {
            try {
                boardGroupInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), params.id])}"
				flash.level = 'danger'
                redirect(action: "edit", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), params.id])}"
			flash.level = 'warning'
            redirect(action: "list")
        }
    }
}
