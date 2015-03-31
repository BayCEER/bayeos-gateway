package gateway

import groovy.sql.Sql

class MessageController {
			
	static transactional = false
	def dataSource
	
	
	def list() {
		
	}
	
	def deleteMessage(){	
		log.debug("getMessage")
		def message = Message.get(params.id)		
		message.delete(flush:true)
	}
		
	def getMessagesByOrigin(){
		log.debug("getMessageByOrigin")		
		def db = new Sql(dataSource)		
		def origin = params.get("origin")
		def max = Math.min(params.max?.toInteger()?:10,100)
		def offset = params.offset?.toInteger()?:0
		
		// log.info(params)
		def mgs = db.rows("select id, result_time, type, content from message where origin like ? order by id LIMIT ? OFFSET ?",[origin,max,offset])		 
		def c =  db.firstRow("select count(*) from message where origin like ?", [origin])
				
				
		render(contentType:"text/json") {
				messages = array {
					for(m in mgs) {
						msg id:m.id, result_time:m.result_time.time,type:m.type,content:m.content
					}
				}
				count = c.count
		}					
		db.close()		
	}


}
