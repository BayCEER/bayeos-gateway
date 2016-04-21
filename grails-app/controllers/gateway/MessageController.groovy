package gateway

import groovy.sql.Sql

import org.apache.tomcat.util.http.ContentType;

import grails.converters.JSON

class MessageController {
			
	static transactional = false
	def dataSource
	
	
	def list() {
		
	}
	
	def delete(){	
		def message = Message.get(params.id)		
		if (message != null) {
			message.delete(flush:true)
			render(status: 200, contentType: "application/json", text:"{\"msg\":\"Message with id:${params.id} deleted.\"}");
		} else {
			render(status: 404, contentType: "application/json", text:"{\"msg\":\"Message with id:${params.id} not found.\"}");						
		}				
		
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
