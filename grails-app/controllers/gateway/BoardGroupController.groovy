package gateway

import groovy.sql.Sql
import grails.converters.JSON

class BoardGroupController {
	
	def dataSource

   
    def index = {
        redirect(action: "list", params: params)
    }

	
	def list() {
		def db = new Sql(dataSource)
		def max = Math.min(params.max?.toInteger()?:10,100)
		def offset = params.offset?.toInteger()?:0
		def result = db.rows("""
					select board_group.id, board_group.name, bg.lrt, bg.count as board_count, group_check.status 
					from (select board_group_id as id, max(last_result_time) as lrt, count(id) as count from board group by board_group_id) bg
					right outer join board_group on (board_group.id = bg.id) 
					join group_check on (board_group.id = group_check.id)
					order by name LIMIT ? OFFSET ?""", [max, offset])
		db.close()
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
		
		def db = new Sql(dataSource)
		def rows = db.rows("SELECT b.id,b.origin,b.name,b.last_rssi, b.last_result_time, chk.status FROM board b, board_check chk where b.id = chk.id and b.board_group_id = ? order by 2 ",[boardGroupInstance.id])
		db.close()

		
		switch (request.method) {
		case 'GET':												
			[boardGroupInstance: boardGroupInstance, boardStatus:rows]		
			break
		case 'POST':			
			if (params.version) {
				def version = params.version.toLong()
				if (boardGroupInstance.version > version) {
					boardGroupInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
							  [message(code: 'boardGroup.label', default: 'BoardGroup')] as Object[],
							  "Another user has updated this BoardGroup while you were editing")
					render view: 'edit', model: [boardGroupInstance: boardGroupInstance,  boardStatus:rows]
					return
				}
			}

			boardGroupInstance.properties = params

			if (!boardGroupInstance.save(flush: true)) {
				render view: 'edit', model: [boardGroupInstance: boardGroupInstance,  boardStatus:rows]
				return
			}

			flash.message = message(code: 'default.updated.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), boardGroupInstance.id])
			redirect action: 'list'
			break
		}
	}
	
	
	def removeBoard(){
		def board = Board.get(params.id)
		if (board) {
			def groupId = board.boardGroup.id						
			board.boardGroup = null			
			try {
				board.save()
			} catch (Exception e){
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'board.label', default: 'Board'), params.id])
				flash.level = 'danger'				
			}
			flash.message = message(code: 'default.updated.message', args: [message(code: 'boardGroup.label', default: 'BoardGroup'), groupId])			
			redirect controller:'boardGroup', action: 'edit', id: groupId						
		} else {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'board.label', default: 'Board'), params.id])
			flash.level = 'warning'		
		}		
	}
	
	
	def addBoard(){		
		[boardGroupInstance: BoardGroup.get(params.id)]				
	}
	
	def addToGroup(){							
		def bg = BoardGroup.get(params.id)
			
		if (params.boards != null && params.boards.size() > 0){	
		params.boards.split(",").each{ it ->
			Board board = Board.get(Integer.valueOf(it))
				if (board){
					board.boardGroup = bg
					board.save()
				}
		} 
		}
				
		redirect  action: 'edit', id: bg.id
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
