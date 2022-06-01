package de.unibayreuth.bayceer.bayeos.gateway.service;

import bayeos.frame.types.LabeledFrame
import bayeos.frame.types.NumberType
import groovy.sql.Sql
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
public class CalculateObsJob implements Runnable {

	@Autowired
	private DataSource dataSource

	@Autowired
	private FrameService frameService

	@Value('${CALC_WAIT_SECS:120}')
	private int waitSecs

	private Logger log = LoggerFactory.getLogger(CalculateObsJob.class)

	@PostConstruct
	public void start(){
		new Thread(this).start()
	}

	@Override
	public void run() {
		Thread.sleep(1000*waitSecs);
		while(true) {
			def exit = -1
			def start = new Date()
			def rowCalculated = 0
			def rowArchived  = 0
			try {
				def db = new Sql(dataSource)
				try {
					log.info("CalculateJob running")
					def ts = new Date().toTimestamp()
					def r = db.firstRow("select max(id) as id from observation")
					if (r == null){
						log.info("Nothing to calculate");
					} else {
						def id = r.id


						db.eachRow("""SELECT c.id, c.db_series_id, c.spline_id, f.name AS f_name, i.name AS i_name, c.filter_critical_values,c.critical_min,c.critical_max
									FROM channel c 
									LEFT JOIN function f ON f.id = c.aggr_function_id 
									LEFT JOIN "interval" i ON i.id = c.aggr_interval_id 
									where c.db_series_id is not null and c.db_exclude_auto_export = false"""){ cha ->

									// Fill table for ExportJopb
									rowCalculated = rowCalculated + db.executeUpdate("""
										insert into observation_calc (channel_id, result_time, result_value)
										select ${cha.id},c.result_time, c.result_value from 
										get_channel_time_value_flag(${ts},${cha.id},${cha.spline_id},${cha.f_name},${cha.i_name},
										${cha.filter_critical_values},${cha.critical_min}, ${cha.critical_max}) as c where c.flag < 1
										""");

									// Move observations to archive table
									if (cha.i_name == null) {
										db.execute("""insert into observation_exp select * from observation where id<=${id} and channel_id = ${cha.id} and result_time<${ts}""")
										db.execute("""delete from observation where id<=${id} and channel_id=${cha.id} and result_time<${ts}""")

									} else {
										db.execute("""insert into observation_exp select * from observation where id<=${id} and channel_id = ${cha.id} and result_time<date_truncate(${ts}, ${cha.i_name}::interval);""")
										db.execute("""delete from observation where id<=${id} and channel_id=${cha.id} and result_time<date_truncate(${ts}, ${cha.i_name}::interval);""")
									}
									rowArchived = rowArchived + db.updateCount
								}
								log.info("${rowCalculated} records calculated.")

					}
					exit = 0
				} catch (SQLException e){
					log.error(e.getMessage())
					exit = -1
				}  finally {
					db.close()
					log.info("CalculateJob finished")
				}
				def millis = (new Date()).getTime() - start.getTime()
				frameService.saveFrame("\$SYS/CalculateJob",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'calculated':${rowCalculated},'archived':${rowArchived},'millis':${millis}}".toString()))
				Thread.sleep(1000*waitSecs);
			} catch (InterruptedException e){
				break			}
		}
	}

}
