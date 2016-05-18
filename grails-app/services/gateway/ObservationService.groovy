package gateway

import java.nio.channels.Channel
import java.sql.SQLException
import java.util.Date

import groovy.sql.Sql


class ObservationService {

	def dataSource
	
	static transactional = false

	
//	def findByIdAndRowId(Integer id, Long rowId) {
//		log.info("findByIdandRowId")						
//		def db = new Sql(dataSource)
//		def List result = new ArrayList()		
//		if (rowId == null){
//			def sql = """select o.id, c.id as channel_id, c.nr as channel_nr, o.result_time , real_value(o.result_value,c.spline_id) as result_value	
//						 from all_observation o join channel c on (o.channel_id = c.id) join board b on (b.id = c.board_id)  
//						 where b.id=:id"""
//			if (Board.get(id).lastResultTime){
//				sql = sql + " and o.result_time > b.last_result_time - '60 min'::interval"				
//			} else {			
//				sql = sql + " and o.result_time > now() - '60 min'::interval"
//			}												
//			result = db.rows(sql + " order by 1 asc;",id:id)
//		} else {			
//			def sql = """select o.id, c.id as channel_id, c.nr as channel_nr, o.result_time , real_value(o.result_value,c.spline_id) as result_value
//				 from all_observation o join channel c on (o.channel_id = c.id) where c.board_id =:id and o.id > :rowId order by 1 asc;"""		    
//			result = db.rows(sql,rowId:rowId,id:id)
//		}
//		db.close()
//		return result
//	}

	def findByIdAndRowId(Integer id, Long rowId) {
		log.info("findByIdandRowId")		
				
		def db = new Sql(dataSource)
		def List result = new ArrayList()
		
		if (rowId == null){
			def lastResult = new java.sql.Timestamp(Board.get(id).lastResultTime.getTime());
			def sql = """select a.* from (
			select o.id, c.id as channel_id, o.result_time , real_value(o.result_value,c.spline_id) as result_value 
			from observation o, channel c where o.result_time > (?::timestamptz - '60 min'::interval)
			and c.id = o.channel_id and c.board_id = ? 
			union 
			select o.id, c.id as channel_id, o.result_time , real_value(o.result_value,c.spline_id) as result_value 
			from observation_exp o, channel c where o.result_time > (?::timestamptz - '60 min'::interval)
			and c.id = o.channel_id and c.board_id = ? 
			) a order by 1 desc limit 600;"""
			result = db.rows(sql, [lastResult, id, lastResult, id]).reverse()
		} else {
			def sql = """select a.* from (
				select o.id, c.id as channel_id, o.result_time , real_value(o.result_value,c.spline_id) as result_value 
				from observation o, channel c where o.id > ? and c.id = o.channel_id and c.board_id = ? 
				union 
				select o.id, c.id as channel_id, o.result_time , real_value(o.result_value,c.spline_id) as result_value 
				from observation_exp o, channel c where o.id > ? and c.id = o.channel_id and c.board_id = ? 
				) a order by 1 desc;"""
			result = db.rows(sql, [rowId,id,rowId,id]).reverse()
		}
		db.close()
		return result
	}
	



}
