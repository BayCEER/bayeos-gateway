package gateway

import groovy.json.JsonBuilder;
import groovy.sql.Sql
import groovy.json.JsonBuilder
import javax.xml.bind.DatatypeConverter


class GrafanaController {
	def dataSource
	
    def index() {	
		// Used to test a connection 
		render "This is a Grafana endpoint"
	}
	
	def search() {		
		log.debug("Search:" + request.JSON) 
		log.debug("Params:" + params)		
		def db = null
		try {
			db = new Sql(dataSource)
			render(contentType: "text/json") {				
					db.eachRow("select path from channel_path order by 1"){ row ->
								element row.path
				   }
			}			
		} catch (Exception e) {
			log.error(e.getMessage())			
		} finally {
			if (db!=null) db.close()
		}
	}
	
	def annotations() {
		log.debug("Annotations:" + request.JSON)
		log.debug("Params:" + params)
	}
	
	def fetchDataPoints(path,from,to,intervals,maxDataPoints){
		log.debug("FetchDataPoints: $path Between:$from and $to")
		def db = null		
		def fromDate = new java.sql.Timestamp(DatatypeConverter.parseDateTime(from).getTimeInMillis())
		def toDate = new java.sql.Timestamp(DatatypeConverter.parseDateTime(to).getTimeInMillis())
		try {
			db = new Sql(dataSource)
			def ret = [] 							
			db.eachRow("""select s.* from (
				with ch as (select channel_id as id, spline_id from channel_path join channel on (channel.id = channel_path.channel_id) where path = :path) ,
					obs as (select real_value(o.result_value,ch.spline_id) as result_value, extract(epoch from date_trunc('milliseconds',o.result_time)) * 1000 as result_time from
					observation o, ch where o.channel_id = ch.id and result_time between :from and :to),
					exp as (select real_value(o.result_value,ch.spline_id) as result_value, extract(epoch from date_trunc('milliseconds',o.result_time)) * 1000 as result_time from
					observation_exp o, ch where o.channel_id = ch.id and result_time between :from and :to)
				select * from exp
				union all
				select * from obs
				) s order by 2 asc;""",path:path,from:fromDate,to:toDate){ r ->
				ret << [r.result_value,r.result_time]
			}		
			return ret
		} catch (Exception e) {
			log.error(e.getMessage())
		} finally {
			if (db!=null) db.close()
		}
		
	}
	
	def query() {
		def req = request.JSON
		log.debug("Query:" + req)
		
		// Query:[maxDataPoints:1629, interval:15s,
		// range:[to:2016-03-17T13:05:01.778Z, from:2016-03-17T07:05:01.776Z],
		// format:json, targets:[[hide:true, target:Test/Lufttemperatur, refId:A]], rangeRaw:[to:now, from:now-6h]]
		def maxDataPoints = req["maxDataPoints"]
		def intervals = req["interval"]
		def to = req["range"]["to"]
		def from = req["range"]["from"]		

		// Result
		// [{"target":"name","datapoints":[[3,1458217462061],[2,1458217412061]]}]
		render(contentType:"text/json") {
			req["targets"].each {
				element(["target":it["target"],datapoints: fetchDataPoints(it["target"],from,to,intervals,maxDataPoints)])				
			}
		}
	}
	
}
