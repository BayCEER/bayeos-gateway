package de.unibayreuth.bayceer.bayeos.gateway.service;

import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import bayeos.frame.types.LabeledFrame
import bayeos.frame.types.NumberType
import de.unibayreuth.bayceer.bayeos.gateway.model.InfluxConnection
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.InfluxConnectionRepository

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.WriteApiBlocking
import com.influxdb.client.domain.WritePrecision

import com.influxdb.client.write.Point
import groovy.sql.Sql
import java.sql.Connection
import org.slf4j.LoggerFactory

@Service
@ConditionalOnProperty(value="INFLUX_EXPORT",matchIfMissing = true, havingValue = "true")
class InfluxExportThread implements Runnable{

    @Value('${INFLUX_WAIT_SECS:120}')
    private int waitSecs

    @Value('${INFLUX_BULK_SIZE:10000}')
    private int influxBulkSize

    @Value('${INFLUX_ORG:bayeos}')
    private String influxOrg

    @Autowired
    private DataSource dataSource

    @Autowired
    FrameService frameService

    @Autowired
    InfluxConnectionRepository repoCon;

    private Logger log = LoggerFactory.getLogger(InfluxExportThread.class)


    public InfluxDBClient createInfluxClient(Long id) throws NoSuchElementException  {
        InfluxDBClient cli = influxClients[id]
        if (cli == null) {
            def ic = repoCon.findById(id).orElseThrow()
            def c = InfluxDBClientFactory.create(ic.url,ic.token.toCharArray(),influxOrg,ic.bucket);
            influxClients[id] = c
            return c
        } else {
            return cli
        }
    }
    
    @Override
    public void run() {
        try {
            log.info("InfluxExportThread started.");
            
            while(true) {
                Thread.sleep(1000*waitSecs)
                def exit = -1
                def start = new Date()
                def records = 0
                def id = 0                
                Connection con = dataSource.getConnection()
                Sql db = new Sql(con)
                def influxClients = [:]
                try {
                    log.info("InfluxExportThread running")
                    db.eachRow(""" select
                                    o.id,
                                    c.name as channel_name,
                                    o.result_time,
                                    o.result_value,
                                    b.influx_measurement,
                                    b.influx_connection_id::int8,
                                    b.name as board_name,
                                    b.origin as board_origin
                                from
                                    observation_out o
                                join channel c on
                                    c.id = o.channel_id
                                join board b on 
                                    c.board_id = b.id
                                order by
                                    o.id asc,
                                    b.influx_connection_id
                                limit ${influxBulkSize}; 
                    """) {  rec ->
                                id = rec.id
                                def cli = influxClients[rec.influx_connection_id]
                                if (cli == null) {
                                    def ic = repoCon.findById(rec.influx_connection_id).orElseThrow()
                                    cli = InfluxDBClientFactory.create(ic.url,ic.token.toCharArray(),influxOrg,ic.bucket);
                                    influxClients[rec.influx_connection_id] = cli
                                }
                                // Incomplete records are skipped
                                if (rec.influx_measurement != null && rec.channel_name != null && rec.board_name!=null) {
                                    WriteApiBlocking writeApi = cli.getWriteApiBlocking()
                                    Point p = Point.measurement(rec.influx_measurement)
                                    p.addField(rec.channel_name, rec.result_value)
                                    p.time(rec.result_time.toInstant(), WritePrecision.MS)
                                    p.addTag("origin",rec.board_origin)
                                    p.addTag("board", rec.board_name)
                                    writeApi.writePoint(p);
                                    records++
                                }
                            }
                    log.info("""${records} records exported""")
                    db.execute("""delete from observation_out where id <= ${id};""")                    
                    exit = 0
                } catch (Exception e){
                    log.error(e.getMessage())
                    exit = -1
                } finally {
                    influxClients.each {  k,c ->
                        ((InfluxDBClient)c).close()
                    }
                    if (con != null) {
                        con.close()
                    }
                }
                def millis = (new Date()).getTime() - start.getTime()
                frameService.saveFrame("\$SYS/InfluxExportThread",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'records':${records},'millis':${millis}}".toString()))
                log.info("InfluxExportThread finished")
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @PostConstruct
    public void start(){
        new Thread(this).start()
    }
}
