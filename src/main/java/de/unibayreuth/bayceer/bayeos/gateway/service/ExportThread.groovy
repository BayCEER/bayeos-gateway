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
import de.unibayreuth.bayceer.bayeos.objekt.ArrayConverter
import de.unibayreuth.bayceer.bayeos.objekt.ArraySerializationException
import de.unibayreuth.bayceer.bayeos.objekt.Art
import de.unibayreuth.bayceer.bayeos.objekt.Messung
import de.unibayreuth.bayceer.bayeos.objekt.MessungOrdner
import de.unibayreuth.bayceer.bayeos.objekt.Node
import de.unibayreuth.bayceer.bayeos.objekt.Objekt



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
                    cli = Client.getInstance()
                    db = new Sql(dataSource)

                    // Authentication user root by ip
                    cli.connect(getExportUrL(),expUser,"")
                    
                    
                    if (expHomeUnitId == null || !cli.nodeExists(expHomeUnitId, Art.MESS_EINHEIT)) {
                        log.info("Set home unit id to root")
                        expHomeUnitId = cli.getRoot(Art.MESS_EINHEIT).id
                    }
                    if (expHomeFolderId == null || !cli.nodeExists(expHomeFolderId, Art.MESSUNG_ORDNER)) {
                        log.info("Set home folder id to root")
                        expHomeFolderId = cli.getRoot(Art.MESSUNG_ORDNER).id
                    }
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
    
    
    private def invalidIds(List<Integer> ids, String art) {       
        Vector idVec = new Vector<Integer>();
        idVec.addAll(ids);
        Vector<Vector<Object>> vec = (Vector<Vector<Object>>) cli.getXmlRpcClient().execute("ObjektHandler.getObjekte",idVec, art);
        for (Iterator<Vector<Object>> it = vec.iterator(); it.hasNext();) {
            try {
                Objekt o =  (Objekt) ArrayConverter.toObject(Objekt.class, it.next().toArray());                
                ids.remove((Object)o.id)                                
            } catch (ArraySerializationException e) {
                throw new XmlRpcException(0, e.getMessage());
            }            
        }         
        return ids        
    }
    
    
       
    
    private def syncDomains() {                     
        db.eachRow("select id from domain where db_folder_id is null"){ it ->            
            syncFolderForDomain(it.id);
        }
    }

    private def syncFolderForDomain(domain_id) {
        try {            
            def dom = db.firstRow("select name from domain where id=${domain_id}")
            def id = cli.newNode(expHomeFolderId, dom.name, Art.MESSUNG_ORDNER).getId()
            log.info("Domain folder: ${dom.name} created")
            db.execute("update domain set db_folder_id = ${id} where id = ${domain_id}")
        } catch (XmlRpcException e) {
            log.error(e.getMessage())
            return false;
        }
        return true;
    }

    private def syncGroups() {
        db.eachRow("select id from board_group where db_folder_id is null"){ it ->            
            syncFolderForGroup(it.id);
        }
    }

    private def syncFolderForGroup(group_id) {        
        def group = db.firstRow("""select g.name, d.id as domain_id, d.db_folder_id as domain_folder_id from board_group g 
				left join domain d on d.id = g.domain_id where g.id =${group_id}""")
        def pFolderId = group.domain_folder_id ?: expHomeFolderId
        try {
            def node = cli.newNode(pFolderId, group.name, Art.MESSUNG_ORDNER)
            log.info("Group folder: ${group.name} created")            		
            MessungOrdner m = cli.getNodeObjekt(node,MessungOrdner.class)
            m.setBeschreibung("Created by BayEOS-Gateway (${hostname})")                        
            cli.updateNodeObjekt(m);                                                                        
            db.execute("update board_group set db_folder_id = ${node.id} where id = ${group_id}")
            return true;
        } catch (XmlRpcException e) {
            log.error(e.getMessage())
            // Fix
            if (!cli.nodeExists(pFolderId,Art.MESSUNG_ORDNER)) {
                if (pFolderId == group.domain_folder_id) {
                    if (syncFolderForDomain(group.domain_id)) {
                        return syncFolderForGroup(group_id)
                    }
                }
            }
            return false;
        }
    }

    private def syncBoards(){
        db.eachRow("SELECT id from board where db_folder_id is null and db_auto_export and name is not null"){ it ->
            syncFolderForBoard(it.id)
        }
    }

    private def syncFolderForBoard(board_id) {
        def board = db.firstRow("""select b.name, b.origin, b.board_group_id, b.domain_id, g.db_folder_id as group_folder_id, d.db_folder_id as domain_folder_id,
                b.lat, b.lon, b.alt from board b left outer join board_group g on g.id = b.board_group_id
				left outer join domain d on d.id = b.domain_id where b.id=${board_id}""")
        def pFolderId = board.group_folder_id ?: board.domain_folder_id ?: expHomeFolderId
        try {
            def node = cli.newNode(pFolderId, board.name, Art.MESSUNG_ORDNER)          		
            log.info("Board folder: ${board.name} created")        
            MessungOrdner m = cli.getNodeObjekt(node,MessungOrdner.class)
            m.setBeschreibung("Created by BayEOS-Gateway (${hostname})")
            m.setLat(board.lat)
            m.setLon(board.lon)
            m.setAlt(board.alt)            
            cli.updateNodeObjekt(m);                                                
            db.execute("update board set db_folder_id = ${m.getId()} where id = ${board_id}")
            return true
        } catch (XmlRpcException e){
            log.error(e.getMessage())
            // Fix
            if (!cli.nodeExists(pFolderId,Art.MESSUNG_ORDNER)) {
                // GW -> D -> B -> C
                if (pFolderId == board.domain_folder_id){
                    if (syncFolderForDomain(board.domain_id)) {
                        return syncFolderForBoard(board_id)
                    }
                    // GW -> G -> B -> C
                } else if (pFolderId == board.group_folder_id) {
                    if (syncFolderForGroup(board.board_group_id)) {
                        return syncFolderForBoard(board_id)
                    }
                }
            }
            return false
        }
    }

    private def syncChannels(){
        db.eachRow("""select c.id from channel c join board b on b.id = c.board_id where b.db_folder_id is not null and c.name is not null and c.db_series_id is null and 
			c.db_exclude_auto_export is false"""){ it ->
                    syncSeriesForChannel(it.id)
                }
    }

    private def syncSeriesForChannel(channel_id) {
        
        def channel = db.firstRow("select c.*, b.db_folder_id from channel c join board b on c.board_id = b.id where c.id=${channel_id}")
        def sId
        try {
            sId = cli.newNode(channel.db_folder_id, channel.name,Art.MESSUNG_MASSENDATEN).getId()
            log.info("Series for channel ${channel.name} created")
        } catch (XmlRpcException e){
            if (!cli.nodeExists(channel.db_folder_id,Art.MESSUNG_ORDNER)) {
                if (syncFolderForBoard(channel.board_id)) {
                    return syncSeriesForChannel(channel.id)
                }
            }
            return false;
        }
        if (channel.unit_id){
            syncChannelUnit(sId, channel.unit_id)
        }
        db.execute("update channel set db_series_id = ${sId} where id = ${channel.id}")
        return true
    }

    private def syncChannelUnit(channel_db_series_id, unit_id){
        def unit = db.firstRow("select * from unit where id = ${unit_id}")
        try {
            if (!cli.nodeRefExists(unit.db_unit_id,channel_db_series_id,Art.MESS_EINHEIT)) {
            	log.info("Reference on unit:${unit.name} for series ${channel_db_series_id} set")
                cli.setNodeReference(unit.db_unit_id,channel_db_series_id,Art.MESS_EINHEIT);
            }                        
        } catch (XmlRpcException e){
            log.error(e.getMessage())
            if (syncUnitForUnit(unit.name, unit_id)){
                return syncChannelUnit(channel_db_series_id, unit_id)
            }
            return false
        }
        return true
    }

    private def syncUnitForUnit(unit_name, unit_id) {
        log.info("syncUnitForUnit:${unit_id}")
        
        try {
            def parentId = expHomeUnitId
            def fId = cli.newNode(parentId, unit_name, Art.MESS_EINHEIT).getId()
            log.info("Unit: ${unit_name} created")
            db.execute("update unit set db_unit_id = ${fId} where id = ${unit_id}")
        } catch (XmlRpcException e){
            log.error(e.getMessage())
            return false
        }
        return true
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
                db.eachRow("""SELECT o.id, c.id as channel_id, c.db_series_id, o.result_time,o.result_value FROM 
							observation_out o join channel c on o.channel_id = c.id where c.db_series_id is not null order by o.id asc limit ${expBulkSize};""") {
                            id = it.id
                            dout.writeInt(it.db_series_id)
                            dout.writeLong(it.result_time.getTime())
                            dout.writeFloat(it.result_value)
                            row++
                        }
                if (row==0) break
                    dout.flush()

                if (!cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[])) {
                    log.error("Export failed")
                    return -1
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
    
    private def syncFolderPropsForBoards() {        
        db.eachRow("""select * from board where db_folder_id is not null and force_sync and db_auto_export;"""){ it ->
                syncFolderPropsForBoard(it)                
        }        
    }
    
    private def syncFolderPropsForBoard(board) {                        
        try {               
                Node node = cli.getNode(board.db_folder_id);
                if (node == null) {
                    log.warn("Failed to resync ${board.id}")
                    return
                }            
                MessungOrdner o = cli.getNodeObjekt(node,MessungOrdner.class)
                o.setBezeichnung(board.name)                
                o.setLat(board.lat)
                o.setLon(board.lon)
                o.setAlt(board.alt) 
                                               
                cli.updateNodeObjekt(o)                               
                                                        
        } catch (XmlRpcException e){
            log.error(e.getMessage())
        } finally {            
            db.execute("update board set force_sync = false where id = ${board.id}")
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
            Node node = cli.getNode(channel.db_series_id)
            if (node == null) {
                log.warn("Failed to resync channel ${channel.id}")
                return
            }
            
            Messung m = cli.getNodeObjekt(node, Messung.class)
            m.setBezeichnung(channel.name)
            cli.updateNodeObjekt(m)
               
            // TBD: Sync units 
            // Get all references 
            // Delete all unit references for series  
            // Add a new unit reference for series           
            
        } catch (XmlRpcException e){
            log.error(e.getMessage())
        } finally {            
            db.execute("update channel set force_sync = false where id = ${channel.id}")
        } 
        return true
    }

   
}

