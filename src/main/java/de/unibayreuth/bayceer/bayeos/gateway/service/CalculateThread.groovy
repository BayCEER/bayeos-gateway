package de.unibayreuth.bayceer.bayeos.gateway.service;

import bayeos.frame.types.LabeledFrame
import bayeos.frame.types.MapUtils
import bayeos.frame.types.NumberType
import bayeos.frame.types.OriginFrame
import bayeos.frame.types.TimestampFrame
import de.unibayreuth.bayceer.bayeos.gateway.event.EventProducer
import de.unibayreuth.bayceer.bayeos.gateway.service.FrameService.Board
import de.unibayreuth.bayceer.bayeos.gateway.event.NewChannelEvent
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.VirtualChannelRepository
import groovy.sql.Sql
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.script.ScriptEngine
import javax.sql.DataSource
import javax.validation.metadata.ValidateUnwrappedValue

import org.slf4j.Logger
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
public class CalculateThread implements Runnable {

    @Autowired
    private DataSource dataSource

    @Autowired
    private FrameService frameService

    @Value('${CALC_WAIT_SECS:120}')
    private int waitSecs

    @Autowired
    VirtualChannelRepository vcRepo

    @Autowired
    ScriptEngine scriptEngine

    @Autowired
    EventProducer eventProducer

    private Logger log = LoggerFactory.getLogger(CalculateThread.class)

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(1000*waitSecs)
                log.info("CalculateThread running")
                def exit = -1
                def start = new Date()
                def rowCalculated = 0
                def rowArchived  = 0
                def rowVC = 0
                def db = new Sql(dataSource)
                try {
                    def ts = new Date().toTimestamp()
                    def r = db.firstRow("select max(id) as id from observation")
                    if (r == null){
                        log.info("Nothing to calculate");
                        continue
                    } else {
                        def id = r.id // observation.id
                        db.eachRow("""SELECT c.id, c.db_series_id, c.spline_id, f.name AS f_name, i.name AS i_name, c.filter_critical_values,c.critical_min,c.critical_max
                                    FROM channel c 
                                    LEFT JOIN function f ON f.id = c.aggr_function_id 
                                    LEFT JOIN "interval" i ON i.id = c.aggr_interval_id
                                    where c.db_series_id is not null and c.db_exclude_auto_export = false 
                                    """){ cha ->

                                    db.withTransaction {
                                        rowCalculated = rowCalculated + db.executeUpdate("""
                                            insert into observation_calc (channel_id, result_time, result_value)
                                            select ${cha.id},c.result_time, c.result_value from 
                                            get_channel_time_value_flag(${ts},${cha.id},${cha.spline_id},${cha.f_name},${cha.i_name},
                                            ${cha.filter_critical_values},${cha.critical_min}, ${cha.critical_max}) as c where c.flag < 1
                                            """);

                                        // Move observations to cache table
                                        if (cha.i_name == null) {
                                            db.execute("""insert into observation_exp select * from observation where id<=${id} and channel_id = ${cha.id} and result_time<${ts}""")
                                            db.execute("""delete from observation where id<=${id} and channel_id=${cha.id} and result_time<${ts}""")
                                        } else {
                                            db.execute("""insert into observation_exp select * from observation where id<=${id} and channel_id = ${cha.id} and result_time<date_truncate(${ts}, ${cha.i_name}::interval);""")
                                            db.execute("""delete from observation where id<=${id} and channel_id=${cha.id} and result_time<date_truncate(${ts}, ${cha.i_name}::interval);""")
                                        }
                                    }
                                    rowArchived = rowArchived + db.updateCount
                                }

                        log.info("${rowCalculated} records calculated.")
                    }

                    // Virtual channel with event=calculate                
                    if (rowCalculated == 0){
                        log.debug("No new data for virtual channels");
                    } else {
                        log.info("Processing virtual channels")
                        db.eachRow("""select distinct vc.nr, vc.id, c.board_id, oc.result_time from observation_calc oc 
                                        join channel c on oc.channel_id = c.id 
                                        join virtual_channel vc on vc.board_id = c.board_id and vc."event" = 'calculate'
                                        join channel_binding cb on vc.id = cb.virtual_channel_id and cb.nr = c.nr order by oc.result_time asc;
                            """){ rec ->                                  
                                        def vc = vcRepo.findById(rec.id).orElse(null)
                                        if (vc == null) {
                                            log.warn("Virtual channel:${rec.id} not found.")
                                            return
                                        }                                        
                                        log.debug("Calculate virtual channel:${vc} on board:${vc.board.origin}")
                                        def values  = [:]
                                        for (b in vc.channelBindings) {
                                            if (b.value == null && b.nr != null) {
                                                def val = db.firstRow("""select oc.result_value from observation_calc oc join channel c on oc.channel_id = c.id
                                                  where oc.result_time = ${rec.result_time} and c.nr = ${b.nr}""")
                                                if (val == null) {
                                                    log.warn("Function value not found.")
                                                    return
                                                }
                                                values[b.nr] = val.result_value
                                            }
                                        }
                                        if (!vc.evaluable(values)) {
                                            log.debug("Virtual function vc:${vc} not evaluable")
                                            return
                                        }
                                        def result = [:]
                                        result[vc.nr] = vc.eval(scriptEngine, values)
                                        log.debug("Input:${MapUtils.toString(values)} Time:${rec.result_time} Result:${MapUtils.toString(result)})")
                                        def frame = new TimestampFrame(rec.result_time,new LabeledFrame(NumberType.Float32,result))
                                        frameService.saveFrame(vc.board.getDomainId(),vc.board.origin,frame.getBytes())
                                        rowVC++
                                }
                    }
                    // Move calc records to observation_out
                    db.withTransaction {  
                        db.execute("insert into observation_out select * from observation_calc")
                        db.execute("delete from observation_calc")                        
                    }
                    exit = 0
                } catch (SQLException e){
                    log.error(e.getMessage())
                    exit = -1
                } finally {
                    db.close()
                    log.info("CalculateThread finished")
                }
                def millis = (new Date()).getTime() - start.getTime()
                frameService.saveFrame("\$SYS/CalculateThread",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'calculated':${rowCalculated},'vc':${rowVC},'archived':${rowArchived},'millis':${millis}}".toString()))
            } catch (InterruptedException e) {
                log.error(e.getMessage())
                break
            }
        }
    }




    @PostConstruct
    public void start(){
        new Thread(this).start()
    }
}



