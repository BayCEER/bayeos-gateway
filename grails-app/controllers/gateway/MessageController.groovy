package gateway

import groovy.sql.Sql
import grails.converters.JSON

class MessageController {
			
	static transactional = false
	def dataSource
	
	
	def list() {
		
	}
	
	def deleteMessage(){	
		log.debug("deleteMessage")
		def message = Message.get(params.id)		
		message.delete(flush:true)
		render ''
	}
		
	def listData(){
		log.debug("listData")
		def origin = params.get("origin")
		def max = params.int('length',10)
		def offset = params.int('start',0)										
		def recordsTotal = Message.countByOrigin(origin) 		
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
		
		
		// Nothing found
		if (recordsTotal == 0) {
			res['recordsFiltered'] = 0
			res['data'] = []

		} else {
			res['recordsFiltered'] = recordsTotal
			def db = new Sql(dataSource)
			def sql = "select id, result_time, type, content from message where origin like ? order by " + sort.join(",") + " LIMIT ? OFFSET ?"
			res['data'] = db.rows(sql,[origin, max, offset])
			db.close()		
		} 

		render res as JSON
						
	}


}
