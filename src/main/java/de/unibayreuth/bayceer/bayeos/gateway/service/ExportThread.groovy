package de.unibayreuth.bayceer.bayeos.gateway.service

import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.xmlrpc.XmlRpcException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import bayeos.frame.types.LabeledFrame
import bayeos.frame.types.NumberType
import de.unibayreuth.bayceer.bayeos.client.Client
import groovy.sql.Sql
import de.unibayreuth.bayceer.bayeos.objekt.ObjektArt


@Service
class ExportThread implements Runnable  {

    private Logger log = LoggerFactory.getLogger(ExportThread.class)

    @Autowired
    FrameService frameService

    @Autowired
    private DataSource dataSource

    @Value('${EXPORT_PROTO:http}')
    private String expProto

    @Value('${EXPORT_HOST:localhost}')
    private String expHost

    @Value('${EXPORT_PORT:5532}')
    private String expPort

    @Value('${EXPORT_CONTEXT:/XMLServlet}')
    private String expContext

    @Value('${EXPORT_USER:root}')
    private String expUser

    @Value('${EXPORT_BULK_SIZE:10000}')
    private int expBulkSize

    @Value('${EXPORT_HOME_UNIT_ID:}')
    private Integer expHomeUnitId

    @Value('${EXPORT_HOME_FOLDER_ID:}')
    private Integer expHomeFolderId

    @Value('${EXPORT_WAIT_SECS:120}')
    private int waitSecs

    private Client cli = null
    private Sql db

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(1000*waitSecs)
                def exit = -1
                def start = new Date()
                def records = 0
                try {
                    log.info("ExportThread running")
                    cli = Client.getInstance()
                    db = new Sql(dataSource)
        
                    // Authentication user root by ip
                    cli.connect(getExportUrL(),expUser,"")
        
                    if (expHomeUnitId == null || !cli.nodeExists(expHomeUnitId, ObjektArt.MESS_EINHEIT)) {
                        log.info("Set home unit id to root")
                        expHomeUnitId = cli.getRoot(ObjektArt.MESS_EINHEIT).id
                    }
                    if (expHomeFolderId == null || !cli.nodeExists(expHomeFolderId, ObjektArt.MESSUNG_ORDNER)) {
                        log.info("Set home folder id to root")
                        expHomeFolderId = cli.getRoot(ObjektArt.MESSUNG_ORDNER).id
                    }
                    createNewFoldersForDomains()
                    createNewFoldersForGroups()
                    createNewFoldersForBoards()
                    createNewSeriesForChannels()
                    records = exportNewObservations()
                    exit = (records<0)?-1:0
                } catch (Exception e){
                    log.error(e.getMessage())
                    exit = -1
                } finally {
                    try{
                        cli.close()
                    } catch(XmlRpcException e){
                        log.error(e.getMessage())
                        exit = -1
                    }
                }
                def millis = (new Date()).getTime() - start.getTime()
                frameService.saveFrame("\$SYS/ExportThread",new LabeledFrame(NumberType.Float32,"{'exit':${exit},'records':${records},'millis':${millis}}".toString()))
                log.info("ExportThread finished")
                        
                
            } catch (InterruptedException e) {
                break;
            }
        
        }
    }


    @PostConstruct
    public void start(){
        new Thread(this).start()
    }

    private def createNewFoldersForDomains() {
        db.eachRow("select id from domain where db_folder_id is null"){ it ->
            createFolderForDomain(it.id);
        }
    }

    private def createFolderForDomain(domain_id) {
        try {
            def dom = db.firstRow("select name from domain where id=${domain_id}")
            def id = cli.newNode(expHomeFolderId, dom.name, ObjektArt.MESSUNG_ORDNER).getId()
            log.info("Created domain: ${dom.name}")
            db.execute("update domain set db_folder_id = ${id} where id = ${domain_id}")
        } catch (XmlRpcException e) {
            log.error(e.getMessage())
            return false;
        }
        return true;
    }

    private def createNewFoldersForGroups() {
        db.eachRow("select id from board_group where db_folder_id is null"){ it ->
            createFolderForGroup(it.id);
        }
    }

    private def createFolderForGroup(group_id) {
        def group = db.firstRow("""select g.name, d.id as domain_id, d.db_folder_id as domain_folder_id from board_group g 
				left join domain d on d.id = g.domain_id where g.id =${group_id}""")
        def pFolderId = group.domain_folder_id ?: expHomeFolderId
        try {
            def id = cli.newNode(pFolderId, group.name, ObjektArt.MESSUNG_ORDNER).getId()
            log.info("Created folder for group: ${group.name}")
            updateFolderDescription(id, group.name, "Created by BayEOS-Gateway (${InetAddress.getLocalHost().getHostName()})".toString())
            db.execute("update board_group set db_folder_id = ${id} where id = ${group_id}")
            return true;
        } catch (XmlRpcException e) {
            log.error(e.getMessage())
            // Fix
            if (!cli.nodeExists(pFolderId,ObjektArt.MESSUNG_ORDNER)) {
                if (pFolderId == group.domain_folder_id) {
                    if (createFolderForDomain(group.domain_id)) {
                        return createFolderForGroup(group_id)
                    }
                }
            }
            return false;
        }
    }

    private def createNewFoldersForBoards(){
        db.eachRow("SELECT id from board where db_folder_id is null and db_auto_export and name is not null"){ it ->
            createFolderForBoard(it.id)
        }
    }

    private def createFolderForBoard(board_id) {
        def board = db.firstRow("""select b.name, b.origin, b.board_group_id, b.domain_id, g.db_folder_id as group_folder_id, d.db_folder_id as domain_folder_id 
				from board b left outer join board_group g on g.id = b.board_group_id
				left outer join domain d on d.id = b.domain_id where b.id=${board_id}""")
        def pFolderId = board.group_folder_id ?: board.domain_folder_id ?: expHomeFolderId
        try {
            def id = cli.newNode(pFolderId, board.name, ObjektArt.MESSUNG_ORDNER).getId()
            log.info("Created folder for board: ${board.name}")
            updateFolderDescription(id, board.name, "Created by BayEOS-Gateway (${InetAddress.getLocalHost().getHostName()}), Board (${board.origin})".toString())
            db.execute("update board set db_folder_id = ${id} where id = ${board_id}")
            return true
        } catch (XmlRpcException e){
            log.error(e.getMessage())
            // Fix
            if (!cli.nodeExists(pFolderId,ObjektArt.MESSUNG_ORDNER)) {
                // GW -> D -> B -> C
                if (pFolderId == board.domain_folder_id){
                    if (createFolderForDomain(board.domain_id)) {
                        return createFolderForBoard(board_id)
                    }
                    // GW -> G -> B -> C
                } else if (pFolderId == board.group_folder_id) {
                    if (createFolderForGroup(board.board_group_id)) {
                        return createFolderForBoard(board_id)
                    }
                }
            }
            return false
        }
    }

    private def createNewSeriesForChannels(){
        db.eachRow("""select c.id from channel c join board b on b.id = c.board_id where b.db_folder_id is not null and c.name is not null and c.db_series_id is null and 
			c.db_exclude_auto_export is false"""){ it ->
                    createSeriesForChannel(it.id)
                }
    }

    private def createSeriesForChannel(channel_id) {
        def channel = db.firstRow("select c.*, b.db_folder_id from channel c join board b on c.board_id = b.id where c.id=${channel_id}")
        def sId
        try {
            sId = cli.newNode(channel.db_folder_id, channel.name,ObjektArt.MESSUNG_MASSENDATEN).getId()
            log.info("Created new series for channel ${channel.name}")
        } catch (XmlRpcException e){
            if (!cli.nodeExists(channel.db_folder_id,ObjektArt.MESSUNG_ORDNER)) {
                if (createFolderForBoard(channel.board_id)) {
                    return createSeriesForChannel(channel.id)
                }
            }
            return false;
        }
        if (channel.unit_id){
            createReferenceForUnit(sId, channel.unit_id)
        }
        db.execute("update channel set db_series_id = ${sId} where id = ${channel.id}")
        return true
    }

    private def createReferenceForUnit(channel_db_series_id, unit_id){
        def unit = db.firstRow("select * from unit where id = ${unit_id}")
        try {
            cli.setNodeReference(unit.db_unit_id,channel_db_series_id,ObjektArt.MESS_EINHEIT)
            log.info("Created reference on unit:${unit.name} for series ${channel_db_series_id}")
        } catch (XmlRpcException e){
            log.error(e.getMessage())
            if (createUnitForUnit(unit.name, unit_id)){
                return createReferenceForUnit(channel_db_series_id, unit_id)
            }
            return false
        }
        return true
    }

    private def createUnitForUnit(unit_name, unit_id) {
        try {
            def parentId = expHomeUnitId
            def fId = cli.newNode(parentId, unit_name, ObjektArt.MESS_EINHEIT).getId()
            log.info("Created unit: ${unit_name}")
            db.execute("update unit set db_unit_id = ${fId} where id = ${unit_id}")
        } catch (XmlRpcException e){
            log.error(e.getMessage())
            return false
        }
        return true
    }

    private def validateChannels(channelIds){
        channelIds.each{ it ->
            def id = it[0]
            def series_id = it[1]
            if (!cli.nodeExists(series_id,ObjektArt.MESSUNG_MASSENDATEN)) {
                createSeriesForChannel(id)
            }
        }
    }

    private def exportNewObservations() {
        log.info("Exporting observations")
        def bout  = new ByteArrayOutputStream(8*1024)
        def dout = new DataOutputStream(bout)
        def id = 0
        def records = 0
        try {
            while (true){
                def row = 0
                Set channelIds = []
                db.eachRow("""SELECT o.id, c.id as channel_id, c.db_series_id, o.result_time,o.result_value FROM 
							observation_out o join channel c on o.channel_id = c.id where c.db_series_id is not null order by o.id asc limit ${expBulkSize};""") {
                            id = it.id
                            channelIds.add([
                                it.channel_id,
                                it.db_series_id
                            ])
                            dout.writeInt(it.db_series_id)
                            dout.writeLong(it.result_time.getTime())
                            dout.writeFloat(it.result_value)
                            row++
                        }
                if (row==0) break
                    dout.flush()

                if (!cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[])) {
                    bout.reset()
                    validateChannels(channelIds)
                    continue;
                }


                log.info("${row} records exported")
                records = records + row
                bout.reset()
                db.execute("delete from observation_out where id <= ?",[id])
            }
        } catch (Exception e){
            log.error(e.getMessage())
            records = -1
        } finally {
            dout.close()
        }
        return records;
    }

    private String getExportUrL() {
        return expProto + "://" + expHost + ":" + expPort + expContext
    }

    private def updateFolderDescription(id,name,desc) {
        cli.getXmlRpcClient().execute("ObjektHandler.updateObjekt",id, ObjektArt.MESSUNG_ORDNER.toString(),[
            name,
            desc,
            null,
            null,
            null,
            null,
            2
        ] as Object[])
    }
}

