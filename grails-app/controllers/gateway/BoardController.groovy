package gateway

import grails.converters.JSON
import grails.converters.XML
import groovy.sql.DataSet
import groovy.sql.Sql
import gateway.time.*

class BoardController {

	def dataSource
	def observationService
	def boardService	
	def springSecurityService

	def chkProps = [
		'samplingInterval',
		'criticalMax',
		'criticalMin',
		'warningMin',
		'warningMax',
		'checkDelay']
	
	def chaProps = chkProps + [
		'nr',
		'label',
		'phenomena',
		'unit',
		'spline',
		'aggrInterval',
		'aggrFunction'
	]

	
	def listData() {
		def search		
		if (params['search[value]']){
			search = "%" + params['search[value]']  + "%"
		}
		def max = params.int('length',10)
		def offset = params.int('start',0)
		def recordsTotal = Board.count()						
		def res = [draw: params["draw"], recordsTotal: recordsTotal]

		// Ordering
		def sort = []
		for (int i = 0; i < 2; i++) {
			def c = params["order[${i}][column]"]
			if (c) {
				sort.add(params["columns[${c}][data]"] + " " + params["order[${i}][dir]"])
			}
		}

		// Default ordering
		if (sort.size() == 0) {
			sort.add("1 desc")
		}
		
		if (recordsTotal == 0) {
			res['recordsFiltered'] = 0
			res['data'] = []
		} else {					
			def rows = boardService.findBoards(search,sort)	
			def size = rows.size()				
			res['recordsFiltered'] = size
			if (size > 0){
				res['data'] = rows[0+offset..size-1].take(max)
			} else {
				res['data'] = []
			}
		}
		render res as JSON
	}
	
	def chartData()  {
		def rowId = params.long("lastRowId")
		def boardId = params.int("boardId")

		def rid = rowId

		render(contentType:"text/json") {
			observations  = array {
				for(o in observationService.findByIdAndRowId(boardId,rowId)) {
					ob bid:boardId, cid:o.channel_id, millis:o.result_time.time,value:o.result_value
					if (o.id > rid) {
						rid = o.id
					}
				}
			}
			lastRowId = rid
		}
	}

	

	def chart() {
			def boardInstance = Board.get(params.id)
			if (!boardInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
				flash.level = 'warning'
				redirect(action: "list")
			} else {
				[boardInstance: boardInstance, channels:boardService.getChannelStati(boardInstance)]
			}
	}

	def index() {
			redirect(action:"list", params:params)
	}

	def list() {



	}
		
		
	def save() {			
			def boardInstance = Board.get(params.id)						
			if (!boardInstance) {
				flash.message = message(code: 'default.not.found.message', args: [
					message(code: 'board.label', default: 'Board'),
					params.id
				])
				flash.level = 'warning'
				redirect action: 'list'
				return
			}
			
			boardInstance.properties = params
			if (boardInstance.save(flush: true)) {
				flash.message = message(code: 'default.updated.message', args: [message(code: 'board.label', default: 'Board'),params.id])
			} else {
				flash.message = message(code: 'default.not.updated.message', args: [message(code: 'board.label', default: 'Board'),params.id])
				flash.level = 'danger'
			}			
			redirect(action: 'edit', params: [id:boardInstance.id])
		}


		def edit() {			
			def boardInstance = Board.get(params.id)			
			if (!boardInstance) {
				flash.message = message(code: 'default.not.found.message', args: [
					message(code: 'board.label', default: 'Board'),
					params.id
				])
				flash.level = 'warning'
				redirect action: 'list'
				return
			}
				
			[boardInstance: boardInstance, channelStati: boardService.getChannelStati(boardInstance)]
		}



		def remove() {
			def boardInstance = Board.get(params.id)
			if (boardInstance) {
				try {
					
					boardInstance.delete(flush: true)
					flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
					redirect(action: "list")
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
					flash.level = 'danger'
					redirect(action: "edit", id: params.id)
				}
			}
			else {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
				flash.level = 'warning'
				redirect(action: "list")
			}

		}



		def saveAsTemplate() {
			def board = Board.get(params.id)
			try {
				def template = boardService.saveAsTemplate(board)
				if 	(template.validate()){
					flash.message = "New board template created."
					redirect(action: "edit", controller:"boardTemplate", id:template.id)
				} else {
					flash.message = "Failed to create a template from board:${board}."
					flash.level = 'danger'
					redirect(action: "edit", id:params.id)
				}


			} catch (Exception e){
				flash.message = "Failed to create a template from board:${board}."
				flash.level = 'danger'
				redirect(action: "edit", id:params.id)
			}
		}

		def applyTemplate() {

			def board = Board.get(params.id)
			def template = BoardTemplate.get(params.templateId)
			try {
				boardService.applyTemplate(board,template)
				flash.message = "Template applied."
				redirect(action: "edit", params:[id:params.id])
			} catch (Exception e){
				flash.message = "Failed to apply template:${template.name} on board:${board}."
				flash.level = 'danger'
				redirect(action: "edit", id:params.id)
			}
		}
		
				

		def editTemplate() {
			def boardInstance = Board.get(params.id)
			if (!boardInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
				flash.level = 'warning'
				redirect(action: "list")
			}

			if (BoardTemplate.count() == 0) {
				flash.message = "Please create a template first."
				flash.level = 'info'
				redirect(action: "edit", params:params)
			}
			[boardInstance: boardInstance]
		}


		def findByOrigin() {
			def board = Board.findByOriginIlike(params.origin + "%")
			if (board) render(board as JSON)
		}


		def toggleNagios() {
			Board b = Board.get(params.id)
			if (b){
				b.excludeFromNagios = (params.enabled == 'false')
				b.save()
			}
			render ''
		}
		
		def createComment() {									
				switch (request.method) {
				case 'GET':
					[commentInstance: new Comment(params), boardId: params.boardId]
					break
				case 'POST':
					def commentInstance = new Comment(params)							
					commentInstance.user = User.get(springSecurityService.principal.id)					
					commentInstance.save()																					
					Board.get(params.boardId).addToComments(commentInstance)																													
					flash.message = message(code: 'default.created.message', args: [message(code: 'comment.label', default: 'Comment'), commentInstance.id])
					redirect action: 'edit', id: params.boardId, fragment: "comments"
					break
				}
		}
		
		def deleteComment(){			
		    Board.get(params.boardId).removeFromComments(Comment.get(params.id))						
			redirect action: 'edit', id: params.boardId, fragment: "comments"			
		}
		
		def editComment(){
			switch (request.method) {
				case 'GET':
					def commentInstance = Comment.get(params.id)
					if (!commentInstance) {
						flash.message = message(code: 'default.not.found.message', args: [message(code: 'comment.label', default: 'Comment'), params.id])
						flash.level = 'danger'
						redirect action: 'edit', id: params.boardId, fragment: "comments"
						return
					}
		
					[commentInstance: commentInstance, boardId: params.boardId]
					break
				case 'POST':
					def commentInstance = Comment.get(params.id)
					if (!commentInstance) {
						flash.message = message(code: 'default.not.found.message', args: [message(code: 'comment.label', default: 'Comment'), params.id])
						flash.level = 'danger'
						redirect action: 'edit', id: params.boardId, fragment: "comments"
						return
					}
							
		
					commentInstance.properties = params
		
					if (!commentInstance.save(flush: true)) {
						flash.message = message(code: 'default.not.saved.message', args: [message(code: 'comment.label', default: 'Comment'), params.id])
						flash.level = 'danger'						
						redirect action: 'edit', id: params.boardId, fragment: "comments"
						return
					}
		
					flash.message = message(code: 'default.updated.message', args: [message(code: 'comment.label', default: 'Comment'), commentInstance.id])
					redirect action: 'edit', id: params.boardId, fragment: "comments"
					break
				}
			
			
		}
			
		

	}
