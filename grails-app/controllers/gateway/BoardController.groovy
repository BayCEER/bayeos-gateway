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

	def chkProps = [
		'samplingInterval',
		'criticalMax',
		'criticalMin',
		'warningMin',
		'warningMax']
	def chaProps = chkProps + [
		'nr',
		'label',
		'phenomena',
		'unit',
		'spline',
		'aggrInterval',
		'aggrFunction'
	]

	def chartData()  {
		def rowId = params.long("lastRowId")
		def boardId = params.int("boardId")

		def cid = rowId

		render(contentType:"text/json") {
			observations  = array {
				for(o in observationService.findByIdAndRowId(boardId,rowId)) {
					ob bid:boardId, ch:o.channel_nr, millis:o.result_time.time,value:o.result_value
					if (o.id > cid) {
						cid = o.id
					}
				}
			}
			lastRowId = cid
		}
	}

	def exportData() {
	}


	def rawData() {
		final String del = ";"
		TimeZone timeZone
		if (params.timeZone) {
			if (TimeZone.getAvailableIDs().contains(params.timeZone)){
				timeZone = TimeZone.getTimeZone(params.timeZone)
			} else {
				render(status:500,text:"Time Zone: ${params.timeZone} not supported.</p>Supported Ids:</p>${TimeZone.getAvailableIDs()}")
				return
			}
		} else {
			timeZone = TimeZone.getTimeZone("GMT+1:00")
		}

		def board = Board.findByOrigin(params.origin)
		def startDate
		if (params.startDate) {
			startDate = params.date('startDate',['yyyyMMddHHmmss'])
		} else {
			startDate = new Date(0)
		}

		def endDate
		if (params.endDate) {
			endDate = params.date('endDate',['yyyyMMddHHmmss'])
		} else {
			endDate = new Date()
		}


		if (params.timeInterval) {
			AbstractTimeInterval ti
			switch (params?.timeInterval) {
				case "Today":
				ti = new Today()
				break
				case "Last24h":
				ti = new Last24h()
				break
				case "Yesterday":
				ti = new Yesterday()
				break
				case "ThisWeek":
				ti = new ThisWeek()
				break
				case "LastWeek":
				ti = new LastWeek()
				break
				case "ThisMonth":
				ti = new ThisMonth()
				break
				case "LastMonth":
				ti = new LastMonth()
				break
				case "ThisYear":
				ti = new ThisYear()
				break
				case "LastYear":
				ti = new LastYear()
				break
				default:
				render(status:500,text:"Time interval: ${params.timeInterval} not supported.")
				return
			}
			startDate = ti.getStartDate()
			endDate = ti.getEndDate()
		}

		if (board==null) {
			render(status:500,text:'Board not found.')
			return
		}
		
		def out = null
		def db = null
		try {
			out = response.outputStream
			db = new Sql(dataSource)
			List chaNr = db.rows("select c.nr,c.label,u.abbrevation, c.spline_id from channel c LEFT OUTER JOIN unit u on (u.id = c.unit_id) where c.board_id = :id order by 1", [id:board.id])
			if (chaNr.size() < 1) {
				render(status:500,text:'Board has no channels.')
				return
			}

			def sql
			if (board.frameStorage) {
				sql = "select result_time, " + chaNr.collect{
					"real_value(c${it[0]},${it[3]})"
				}.join(",") + " from _board_data_${board.id} where result_time >= '${startDate.format('yyyy-MM-dd HH:mm:ss',timeZone)}' and result_time <= '${endDate.format('yyyy-MM-dd HH:mm:ss',timeZone)}' order by result_time, id"
			} else {
				sql = "select * from crosstab('select result_time, c.nr, real_value(o.result_value,c.spline_id) from all_observation o, channel c where " +
				"o.channel_id = c.id and c.board_id = ${board.id} and o.result_time >= ''${startDate.format('yyyy-MM-dd HH:mm:ss',timeZone)}'' and o.result_time <= ''${endDate.format('yyyy-MM-dd HH:mm:ss', timeZone)}'' order by o.result_time, c.nr, o.id', 'SELECT nr FROM channel where board_id = ${board.id} ORDER BY 1')" +
				" as ct(result_time timestamp with time zone ,"	+ chaNr.collect{
					"c${it[0]} text"
				}.join(", ") + ");"
			}
			
			
			withFormat {
				// default format
				csv {					
					if (params.header) {
						out << "Date"
						chaNr.each{
							out << del
							out << ((it[1]==null)?"Channel ${it[0]}":it[1])							
							out << ((it[2]==null)?"[]":"[${it[2]}]")
							 
						}
						out << "\n"
					}
					db.eachRow(sql){ row ->
						out << (row.result_time).format('yyyy-MM-dd HH:mm:ss.SSS', timeZone)
						chaNr.each{
							out << del
							if (row[it[0]]) {
								out << row[it[0]]
							}
						}
						out << "\n"
					}					
				}

				xls {					
					response.setHeader("Content-disposition", "attachment; filename=${board.origin}.csv")
					response.contentType = "application/vnd.ms-excel"
					out << "sep=${del}\n"
					if (params.header) {
						out << "Date"
						chaNr.each{
							out << del
							out << ((it[1]==null)?"Channel ${it[0]}":it[1])							
							out << ((it[2]==null)?"[]":"[${it[2]}]")
						}
						out << "\n"
					}
					db.eachRow(sql){ row ->
						out << ((Date)row.result_time).format('yyyy-MM-dd HH:mm:ss.SSS', timeZone)
						chaNr.each{
							out << del
							if (row[it[0]]) {
								out << row[it[0]]
							}
						}
						out << "\n"
					}					
				}

				xml {
					render db.rows(sql) as XML
				}
				json {
					render db.rows(sql) as JSON
				}
			}
			
			
		} catch (IOException e) {
			if (out!=null) {
				out.close()								
			}
		} finally {
			if (db!=null) db.close()
		}
		








		}




		def chart() {
			def boardInstance = Board.get(params.id)
			if (!boardInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
				flash.level = 'warning'
				redirect(action: "list")
			} else {
				def channels  = Channel.findAllByBoard(boardInstance, [sort:"nr"])
				def channelList = []
				channels.each{
					channelList.add(it.nr)
				}

				[boardInstance: boardInstance, channels:channels, channelList: channelList]
			}
		}

		def index() {
			redirect(action:"list", params:params)
		}

		def list() {

			def max = Math.min(params.max?.toInteger()?:10,100)
			def offset = params.offset?.toInteger()?:0
			def group = BoardGroup.findByName(params?.group)

			def groups = BoardGroup.listOrderByName()*.name
			groups.add(0,"All")

			if (group != null) {
				log.debug("Fetch boards for group:" + group.id)
				[groups: groups, group:group.name, result: boardService.findBoardsByGroup(group.id, max, offset), total:Board.findAllByBoardGroup(group).size(), offset: offset]
			} else {
				log.debug("Fetch all boards")
				[groups: groups, group:'All' , result: boardService.findAllBoards(max, offset), total:Board.count(), offset: offset]
			}

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


			switch (request.method) {
				case 'GET':
					[boardInstance: boardInstance, channelStati: boardService.getChannelStati(boardInstance), boardStatus:boardService.getBoardStatus(boardInstance), offset: params.offset, group: params.group]
					break
				case 'POST':
				if (params.version) {
					def version = params.version.toLong()
					if (boardInstance.version > version) {
						boardInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
						[
							message(code: 'unit.label', default: 'Unit')] as Object[],
						"Another user has updated this Board while you were editing")
						render view: 'edit', model: [boardInstance: boardInstance, channelStati: boardService.getChannelStati(boardInstance), boardStatus:boardService.getBoardStatus(boardInstance),offset: params.offset, group: params.group]
						return
					}
				}

				boardInstance.properties = params

				if (!boardInstance.save(flush: true)) {
					render view: 'edit', model: [boardInstance: boardInstance, channelStati: boardService.getChannelStati(boardInstance), boardStatus:boardService.getBoardStatus(boardInstance),offset: params.offset, group: params.group]
					return
				}

				flash.message = message(code: 'default.updated.message', args: [
					message(code: 'board.label', default: 'Board'),
					boardInstance.id
				])
				redirect(action: 'list', params: [offset:params.offset, group: params.group])
				break
			}
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
					flash.message = 'error'
					redirect(action: "edit", id: params.id)
				}
			}
			else {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'board.label', default: 'Board'), params.id])}"
				flash.message = 'warning'
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

			def board = Board.findByOriginIlikeAndFrameStorage(params.origin + "%", true)
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



	}
