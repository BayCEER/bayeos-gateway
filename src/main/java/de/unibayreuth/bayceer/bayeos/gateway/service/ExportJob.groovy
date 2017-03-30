package de.unibayreuth.bayceer.bayeos.gateway.service

import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import de.unibayreuth.bayceer.bayeos.client.Client
import de.unibayreuth.bayceer.bayeos.objekt.ObjektNodeModel
import groovy.sql.Sql
import org.apache.xmlrpc.XmlRpcException
import java.sql.SQLException

@Component
@Profile("default")
class ExportJob implements Runnable  {

    
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
	
	@Value('${EXPORT_WAIT_SECS:120}')
	private int expWaitSecs	
	
	@Value('${EXPORT_BULK_SIZE:10000}')
	private int expBulkSize
	
	@Value('${EXPORT_HOME_UNIT_ID:}')
	private Integer expHomeUnitId
	
	@Value('${EXPORT_HOME_FOLDER_ID:}')
	private Integer expHomeFolderId
	
	private Client cli = null
	private ObjektNodeModel om = null
	private Sql db
	
	private Logger log = Logger.getLogger(ExportJob.class)
	
	private String getExportUrL() {
		return expProto + "://" + expHost + ":" + expPort + expContext
	}
	
	@PostConstruct
	public void start(){
		new Thread(this).start()
	}
						
	@Override
	public void run() {
		Thread.sleep(1000*expWaitSecs);
		while(true) {
			try {
			log.info("ExportJob running")
			cli = Client.getInstance()			
			db = new Sql(dataSource)												
			// Stats			 			 			
			def rows  = db.executeInsert "INSERT INTO export_job_stat (start_time) values (${new Date().toTimestamp()});"							
			def id = rows[0][0]
			try {				
				// Authentication user root by ip 
				cli.connect(getExportUrL(),expUser,"")				
				om = new ObjektNodeModel()			
				
				def config = db.firstRow("select * from export_job_config")
				
				// Variable overwrite							
				if (expHomeUnitId != null) {
					config.db_home_unit_id = expHomeUnitId
				}				
				  				
				// Set unit destination to root unit when not set
				if (config.db_home_unit_id == null) {
					config.db_home_unit_id = om.getRoot(ObjektArt.MESS_EINHEIT).id
					if (config.db_home_unit_id != null){
						db.execute "UPDATE export_job_config set db_home_unit_id = ${config.db_home_unit_id};"
					}										
				}				
				
				if (config.db_home_unit_id) {
					syncUnits(config.db_home_unit_id)
				}

				// Variable overwrite
				if (expHomeFolderId != null) {
					config.db_home_folder_id = expHomeFolderId
				}
				
				// Set folder destination to root folder when not set
				if (config.db_home_folder_id == null) {
					config.db_home_folder_id = om.getRoot(ObjektArt.MESSUNG_ORDNER).id
					if (config.db_home_folder_id != null){
						db.execute "UPDATE export_job_config set db_home_folder_id = ${config.db_home_folder_id};"
					}					
				}
				
				if (config.db_home_folder_id) {
					syncBoards(config.db_home_folder_id)
				}				
				
				// Data				
				exportObs(id)
				
				// Stats
				db.execute "UPDATE export_job_stat set status=0 WHERE id = $id;"
															
			} catch (Exception e){
				db.execute "UPDATE export_job_stat set status=1 WHERE id = $id;"
				log.error(e.getMessage())				
			} finally {						
				try{cli.close()} catch(XmlRpcException e){}
				try{					
					db.execute "UPDATE export_job_stat set end_time = ${new Date().toTimestamp()} WHERE id = $id;"
					db.close()
				} catch(SQLException e){}				
				log.info("ExportJob finished")
			}
												
				Thread.sleep(1000*expWaitSecs)
			} catch (InterruptedException e) {
				break;
		}
		}
	}
	
	
	
	private def syncUnits(Integer dbHomeUnitId) {
		try {
			db.eachRow("SELECT id, name from unit where db_unit_id is null"){ it ->
				log.info("Syncing unit: ${it.name}")
				Integer dbUnitId = om.newNode(dbHomeUnitId, it.name, ObjektArt.MESS_EINHEIT).getId()
				db.executeUpdate("update unit set db_unit_id = ? where id = ?",[dbUnitId, it.id])
			}
		} catch (Exception e){
			log.error(e.getMessage())
		}
	}

	private def syncBoards(Integer dbHomeFolderId) {
		try {
						
			db.eachRow("""SELECT b.id, b.name, b.origin, b.db_folder_id as board_folder_id, bg.db_folder_id as group_folder_id 
				from board b left outer join board_group bg on (b.board_group_id = bg.id) where b.db_auto_export and b.name is not null order by b.id"""){ it ->						
				log.info("Syncing board: ${it.name}")				
				Integer fId = it.board_folder_id
				if (fId == null){
					Integer pId = it.group_folder_id ?: dbHomeFolderId					
					if (pId != null){
						fId = om.newNode(pId, it.name, ObjektArt.MESSUNG_ORDNER).getId()												
						def desc = "Created by BayEOS-Gateway (${InetAddress.getLocalHost().getHostName()}), Board (${it.origin})"						 											
						cli.getXmlRpcClient().execute("ObjektHandler.updateObjekt",fId,ObjektArt.MESSUNG_ORDNER.toString(),[it.name,desc.toString(),null,null,null,null,2] as Object[])					
						db.executeUpdate("update board set db_folder_id = ? where id = ?",fId,it.id)
						log.info("Updated folder id for board: ${it.name}")
					} else {
						log.info("Failed to sync board: ${it.name} due to missing home folder")
						return
					}					
				}												
				if (fId!=null){
					syncChannels(it.id, fId)
				}
			}
		} catch (Exception e){
			log.error(e.getMessage())
		}
	}

	private def syncChannels(Long boardId, Integer dbFolderId){
		try {
			
			db.eachRow("""select c.id, c.name, u.db_unit_id, CASE WHEN c.aggr_interval_id is not null THEN extract(EPOCH from i.name::interval)::int ELSE COALESCE(b.sampling_interval,c.sampling_interval) END as sampling_interval
					from channel c JOIN board b on (b.id = c.board_id) LEFT JOIN unit u on u.id = c.unit_id LEFT JOIN interval i on (i.id = c.aggr_interval_id)
					where c.board_id = ? and c.db_series_id is null and c.name is not null and (c.db_exclude_auto_export is null or c.db_exclude_auto_export is false) order by c.nr asc""",[boardId]){ it ->																										
				log.info("Syncing channel: ${it.name}")
				Integer id = (om.newNode(dbFolderId, it.name, ObjektArt.MESSUNG_MASSENDATEN)).getId()
				cli.getXmlRpcClient().execute("ObjektHandler.updateObjekt",id,ObjektArt.MESSUNG_MASSENDATEN.toString(),[it.name,"Created by Gateway",it.sampling_interval,null,null,1,2] as Object[])
				db.executeUpdate("update channel set db_series_id = ? where id = ?",id,it.id)
				log.info("Updated db series id for channel: ${it.name}")
				if (it.db_unit_id != null){
					om.setNodeReference(it.db_unit_id,id,ObjektArt.MESS_EINHEIT)
				}
			}

		} catch (Exception e){
			log.error(e.getMessage())
		}
	}
	
	private def exportObs(Long statId) {
		log.info("Exporting observations")	
		def ts = new Date().toTimestamp()
		def bout  = new ByteArrayOutputStream(8*1024)
		def dout = new DataOutputStream(bout)		
		Integer exported = 0		
							 
		try {																				
			def id = db.firstRow("select max(id) from observation")
			if (id == null){
				log.info("Nothing to export");
				db.executeUpdate("update export_job_stat set exported = 0 where id = ?",statId)								
			} else {
				int row = 0
				db.eachRow("SELECT * from get_bayeos_obs(${ts})"){
					dout.writeInt(it.db_series_id)
					dout.writeLong(it.result_time.getTime())
					dout.writeFloat(it.result_value)
					row++
					if (row%expBulkSize==0){
						log.info("Exporting ${expBulkSize} observations")
						cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[]);
						exported += expBulkSize
						db.executeUpdate("update export_job_stat set exported = ? where id = ?",exported,statId)												
						bout.reset()
						row = 0						
					}
				}
				dout.flush()
				if (row > 0) {
					log.info("Exporting ${row} observations")
					cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[]);
					exported += row					
				}
				db.executeUpdate("update export_job_stat set exported = ? where id = ?",exported,statId)
				log.info("Move records to archive table.")
				db.call("{ call delete_obs(?,?) }",[ts,id.max])							
			}																								
		} catch (Exception e){
			log.error(e.getMessage())
		} finally {
			dout.close()			
		}		
	}
}
