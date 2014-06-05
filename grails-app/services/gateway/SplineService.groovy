package gateway

import groovy.sql.Sql

class SplineService {
	def dataSource
	
	def getSplinesWithLockedFlag(Integer max, Integer offset) {		
		def db = new Sql(dataSource)
		def splines = db.rows("select s.id, s.name, c.id is not null locked from spline s left outer join channel c on c.spline_id = s.id order by s.name LIMIT ? OFFSET ?",[max, offset])
		db.close()
		return splines		
	}
	
}
