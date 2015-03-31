package gateway

import org.springframework.dao.DataIntegrityViolationException

class UserController {



    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':			
			def userInstance = new User()
        	[userInstance: userInstance]
			break
		case 'POST':
			println(params)
			def userInstance = new User(params)
			User.withTransaction {																				
						if (userInstance.hasErrors() || !userInstance.save()) {
							render view: 'create', model: [userInstance: userInstance]
							return							
						}
						
						params?.roles.each{ roleId ->							
													 						
							UserRole.create(userInstance,Role.get(roleId))
						}				
			}
						
			
			flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
	        redirect action: 'list'
			break
		}
    }
    
    def edit() {			
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])
			flash.level = 'danger'
			redirect action: 'list'
			return
		}
		
		switch (request.method) {
		case 'GET':	     	        
	        [userInstance: userInstance]
			break
		case 'POST':	        

	        if (params.version) {
	            def version = params.version.toLong()
	            if (userInstance.version > version) {
	                userInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'user.label', default: 'User')] as Object[],
	                          "Another user has updated this User while you were editing")
	                render view: 'edit', model:[userInstance: userInstance]
	                return
	            }
	        }

	        userInstance.properties = params
			
			if (userInstance.hasErrors() || !userInstance.save()) {
					render view: 'edit', model: [userInstance: userInstance]
					return
			}
			
			// BUG#73
			UserRole.removeAll(userInstance)
			params.roles.each{ roleId ->															
					UserRole.create(userInstance,Role.get(roleId))
			}
	        
			
			
			flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
	        redirect action: 'list'
			break
		}
    }

    def delete() {
        def userInstance = User.get(params.id)
        if (!userInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])
			flash.level = 'danger'
            redirect action: 'list'
            return
        }

        try {
            userInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])
			flash.level = 'danger'
            redirect action: 'list'
        }
    }
}
