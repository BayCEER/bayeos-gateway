package de.unibayreuth.bayceer.bayeos.gateway.service


import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import de.unibayreuth.bayceer.bayeos.frame.types.LabeledFrame
import de.unibayreuth.bayceer.bayeos.frame.types.NumberType
import de.unibayreuth.bayceer.bayeos.api.client.BayeosClient
import de.unibayreuth.bayceer.bayeos.api.client.exception.BayeosException
import de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind


import groovy.sql.Sql



@Service
class ExportThread implements Runnable  {

    private Logger log = LoggerFactory.getLogger(ExportThread.class)

    @Autowired
    FrameService frameService

    @Autowired
    private DataSource dataSource

    @Value('${GW_EXPORT_PROTO:http}')
    private String expProto

    @Value('${GW_EXPORT_HOST:localhost}')
    private String expHost

    @Value('${GW_EXPORT_PORT:5532}')
    private String expPort

    @Value('${GW_EXPORT_CONTEXT:/bayeos-api}')
    private String expContext

    @Value('${GW_EXPORT_USER:root}')
    private String expUser

    @Value('${GW_EXPORT_PASSWORD:}')
    private String expPassword

    @Value('${GW_EXPORT_HOME_PATH:/}')
    private String expHomePath

    @Value('${GW_EXPORT_BULK_SIZE:10000}')
    private int expBulkSize

    @Value('${GW_EXPORT_WAIT_SECS:120}')
    private int waitSecs

    private BayeosClient cli = null
    private Sql db

    private String hostname = ""

    @Override
    public void run() {

        try {
            hostname = InetAddress.getLocalHost().getHostName()
        } catch (UnknownHostException e) {
            log.error("Hostname lookup failed")
        }
        while(true) {
            try {
                Thread.sleep(1000*waitSecs)
                def exit = -1
                def start = new Date()
                def records = 0
                try {
                    log.info("ExportThread running")
                    db = new Sql(dataSource)
                    connect()
                    syncDomains()
                    syncGroups()
                    syncBoards()
                    syncChannels()
                    syncFolderPropsForBoards()
                    syncFolderPropsForChannels()
                    records = exportNewObservations()
                    exit = (records<0)?-1:0
                } catch (Exception e){
                    log.error(e.getMessage())
                    exit = -1
                } finally {
                    try {
                        db?.close()
                    } catch (Exception e) {
                        log.error(e.getMessage())
                    }
                }
                def millis = (new Date()).getTime() - start.getTime()
                frameService.saveFrame("\$SYS/ExportThread",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'records':${records},'millis':${millis}}".toString()))
                log.info("ExportThread finished")
            } catch (InterruptedException e) {
                break;
            }
        }
        try {
            cli?.close()
        } catch (Exception e) {
            log.error(e.getMessage())
        }
    }


    @PostConstruct
    public void start(){
        if (!expUser?.trim() || !expPassword){
            log.error("EXPORT_USER or EXPORT_PASSWORD not configured - ExportThread not started")
            return
        }
        new Thread(this).start()
    }

    private void connect(){
        if (cli == null){
            cli = BayeosClient.builder()
                    .baseUrl("${expProto}://${expHost}:${expPort}${expContext}")
                    .credentials(expUser, expPassword)                    
                    .build()
        }
        
    }

    private def syncDomains() {
        db.eachRow("select id, name from domain where db_folder_id is null"){ it ->
            syncFolderForDomain(it.id, it.name)
        }
    }

    private def syncFolderForDomain(domain_id, name) {
        try {
            def node = cli.nodes().createPath(nodePath(name), NodeKind.FOLDER).execute()
            log.info("Domain folder: ${name} created")
            db.execute("update domain set db_folder_id = ? where id = ?", [node.id, domain_id])
        } catch (BayeosException e) {
            log.error("Failed to sync domain ${name}: ${e.message}")
            return false;
        }
        return true;
    }

    private def syncGroups() {
        db.eachRow("""select g.id, g.name, d.name as domain_name from board_group g
                left join domain d on d.id = g.domain_id where g.db_folder_id is null"""){ it ->
            syncFolderForGroup(it.id, it.domain_name, it.name)
        }
    }

    private def syncFolderForGroup(group_id, domain_name, group_name) {
        try {
            def node = cli.nodes().createPath(nodePath(domain_name, group_name), NodeKind.FOLDER)
                    .metadata("description", gatewayDescription())
                    .execute()
            log.info("Group folder: ${group_name} created")
            db.execute("update board_group set db_folder_id = ? where id = ?", [node.id, group_id])
            return true;
        } catch (BayeosException e) {
            log.error("Failed to sync board group ${group_name}: ${e.message}")
            return false;
        }
    }

    private def syncBoards(){
        db.eachRow("""select b.id, b.name, b.lat, b.lon, b.alt, g.name as group_name, d.name as domain_name
                from board b
                left outer join board_group g on g.id = b.board_group_id
                left outer join domain d on d.id = b.domain_id
                where b.db_folder_id is null and b.db_auto_export and b.name is not null"""){ it ->
            syncFolderForBoard(it)
        }
    }

    private def syncFolderForBoard(board) {
        try {
            def builder = cli.nodes().createPath(nodePath(board.domain_name, board.group_name, board.name), NodeKind.FOLDER)
                    .metadata("description", gatewayDescription())
            if (board.lat != null || board.lon != null || board.alt != null) {
                builder.metadata("location", [lat: board.lat, lon: board.lon, alt: board.alt])
            }
            def node = builder.execute()
            log.info("Board folder: ${board.name} created")
            db.execute("update board set db_folder_id = ? where id = ?", [node.id, board.id])
            return true
        } catch (BayeosException e){
            log.error("Failed to sync board ${board.name}: ${e.message}")
            return false
        }
    }

    private def syncChannels(){
        db.eachRow("""select c.id, c.name, c.unit_id, b.name as board_name, g.name as group_name, d.name as domain_name
                from channel c
                join board b on b.id = c.board_id
                left outer join board_group g on g.id = b.board_group_id
                left outer join domain d on d.id = b.domain_id
                where b.db_folder_id is not null and c.name is not null and c.db_series_id is null and
                c.db_exclude_auto_export is false"""){ it ->
            syncSeriesForChannel(it)
        }
    }

    private def syncSeriesForChannel(channel) {
        try {
            def builder = cli.timeseries().create(channel.name)
                    .path(nodePath(channel.domain_name, channel.group_name, channel.board_name, channel.name))
            def unit = unitAbbrevation(channel.unit_id)
            if (unit) {
                builder.metadata("unit", unit)
            }
            def node = builder.execute()
            log.info("Series for channel ${channel.name} created")
            db.execute("update channel set db_series_id = ? where id = ?", [node.id, channel.id])
            return true
        } catch (BayeosException e){
            log.error("Failed to sync channel ${channel.name}: ${e.message}")
            return false;
        }
    }

    private String unitAbbrevation(unit_id) {
        if (unit_id == null) {
            return null
        }
        def unit = db.firstRow("select abbrevation from unit where id = ?", [unit_id])
        return unit?.abbrevation?.trim() ?: null
    }

    private def exportNewObservations() {
        log.info("Exporting observations")
        def records = 0
        try {
            while (true){
                def rows = db.rows("""SELECT o.id, c.db_series_id, o.result_time, o.result_value FROM
                            observation_out o join channel c on o.channel_id = c.id
                            where c.db_series_id is not null order by o.id asc limit ${expBulkSize};""")
                if (rows.isEmpty()){
                    break
                }
                def batch = cli.timeseries().writeBatch().chunkSize(expBulkSize)
                rows.groupBy { it.db_series_id }.each { series_id, seriesRows ->
                    def scope = batch.series(series_id)
                    seriesRows.each { r ->
                        scope.point(r.result_time.toInstant(), (double) r.result_value)
                    }
                }
                batch.send()
                def last = rows.last().id
                log.info("${rows.size()} records exported")
                records = records + rows.size()
                db.execute("delete from observation_out where id <= ?", [last])
            }
        } catch (Exception e){
            log.error(e.getMessage())
            records = -1
        }
        return records;
    }

    

    private def syncFolderPropsForBoards() {
        db.eachRow("""select * from board where db_folder_id is not null and force_sync and db_auto_export;"""){ it ->
            syncFolderPropsForBoard(it)
        }
    }

    private def syncFolderPropsForBoard(board) {
        try {
            def metadata = [description: gatewayDescription()]
            if (board.lat != null || board.lon != null || board.alt != null) {
                metadata["location"] = [lat: board.lat, lon: board.lon, alt: board.alt]
            }
            cli.nodes().update(board.db_folder_id, [display_name: board.name, metadata: metadata])
        } catch (BayeosException e){
            log.error("Failed to resync board ${board.id}: ${e.message}")
        } finally {
            db.execute("update board set force_sync = false where id = ?", [board.id])
        }
        return true
    }

    private def syncFolderPropsForChannels() {
        db.eachRow("""select c.* from channel c join board b on b.id = c.board_id where b.db_folder_id is not null and b.db_auto_export
            and c.db_exclude_auto_export is false and c.db_series_id is not null and c.force_sync;"""){ it ->
            syncFolderPropsForChannel(it)
        }
    }

    private def syncFolderPropsForChannel(channel) {
        try {
            cli.timeseries().update(channel.db_series_id, [display_name: channel.name])
        } catch (BayeosException e){
            log.error("Failed to resync channel ${channel.id}: ${e.message}")
        } finally {
            db.execute("update channel set force_sync = false where id = ?", [channel.id])
        }
        return true
    }

    private String gatewayDescription(){
        return "Created by BayEOS-Gateway (${hostname})".toString()
    }

    private String nodePath(String... names){
        def sb = new StringBuilder(homePath())
        names.each { name ->
            def segment = segment(name)
            if (segment) {
                sb.append('/').append(segment)
            }
        }
        def path = sb.toString()
        if (!path.startsWith('/')) {
            path = '/' + path
        }
        return path
    }

    private String homePath(){
        def home = expHomePath?.trim()
        if (!home || home == '/') {
            return ''
        }
        return home.endsWith('/') ? home.substring(0, home.length()-1) : home
    }

    private static String segment(name){
        if (name == null){
            return null
        }
        def s = name.trim().replace('/', ',')
        return s ? s : null
    }


}
