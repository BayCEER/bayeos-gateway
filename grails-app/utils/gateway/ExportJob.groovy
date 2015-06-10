package gateway

import groovy.sql.Sql
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Vector
import java.lang.Byte

import org.apache.xmlrpc.XmlRpcException

import de.unibayreuth.bayceer.bayeos.client.Client
import de.unibayreuth.bayceer.bayeos.objekt.ObjektArt
import de.unibayreuth.bayceer.bayeos.objekt.ObjektNodeModel


class ExportJob implements Runnable  {

	private Client cli = null
	
	private ObjektNodeModel om = null	
	private ExportJobConfig config
	
	def db
	def dataSource
	

	public ExportJob(dataSource,ExportJobConfig config){
		this.dataSource = dataSource
		this.config = config
	}

	@Override
	public void run() {
		while(true) {
			try {
			log.info("ExportJob running")
			def cli = Client.getInstance()			
			def db = new Sql(dataSource)												
			// Stats			 			 			
			def rows  = db.executeInsert "INSERT INTO export_job_stat (start_time) values (${new Date().toTimestamp()});"							
			def id = rows[0][0]
			try {				
				cli.connect(config.url.toString(),config.userName,config.password)				
				om = new ObjektNodeModel()				

  
				// Set unit destination to root unit when not set
				if (config.dbHomeUnitId == null) {
					config.dbHomeUnitId = om.getRoot(ObjektArt.KEY_MESS_EINHEIT).id
					config.save()					
				}				
				if (config.dbHomeUnitId) {
					syncUnits(config.dbHomeUnitId)
				}

				// Set folder destination to root folder when not set
				if (config.dbHomeFolderId == null) {
					config.dbHomeFolderId = om.getRoot(ObjektArt.KEY_MESSUNG_ORDNER).id
					config.save()	
				}
				
				if (config.dbHomeFolderId) {
					syncBoards(dbHomeFolderId)
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
									
			
				Thread.sleep(1000*60* config.sleepInterval)
			} catch (InterruptedException e) {
			break;
		}
		}
	}


	private def syncUnits(Integer dbHomeUnitId) {
		try {
			def rows = db.rows("SELECT id, name from unit where db_unit_id is null")
			log.info("Syncing ${rows.size} units")
			rows.each {
				Integer dbUnitId = om.newNode(dbHomeUnitId, it.name, ObjektArt.MESS_EINHEIT).getId()
				db.executeUpdate("update unit set db_unit_id = ? where id = ?",[dbUnitId, it.id])
			}
		} catch (Exception e){
			log.error(e.getMessage())
		}
	}

	private def syncBoards(Integer dbHomeFolderId) {
		try {
			def rows = db.rows("SELECT id, name, db_folder_id from board where board.db_auto_export and name is not null order by id")
			log.info("Syncing ${rows.size} boards")
			rows.each{
				log.info("Syncing board: ${it.name}")
				Integer fId = it.db_folder_id
				if (fId == null && dbHomeFolderId != null){
					fId = om.newNode(dbHomeFolderId, it.name, ObjektArt.MESSUNG_ORDNER).getId()
					db.executeUpdate("update board set db_folder_id = ? where id = ?",fId,it.id)
					log.info("Updated folder id for board: ${it.name}")
				} else if (fId == null && dbHomeFolderId == null){
					log.info("Failed to sync board: ${it.name} due to missing gateway home folder")
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
			
			def rows = db.rows("""select c.id, c.label, u.db_unit_id,
			CASE WHEN c.aggr_interval_id is not null THEN extract(EPOCH from i.name::interval)::int ELSE d.sampling_interval END
			from check_device d, channel c LEFT JOIN unit u on u.id = c.unit_id LEFT JOIN interval i on i.id = c.aggr_interval_id
			where c.board_id = ? and c.db_series_id is null and d.channel_id = c.id
			and c.label is not null and (c.db_exclude_auto_export is null or c.db_exclude_auto_export is false) order by c.nr asc""",[boardId])
												
			rows.each{								
				log.info("Syncing channel: ${it.label}")
				Integer id = (om.newNode(dbFolderId, it.label, ObjektArt.MESSUNG_MASSENDATEN)).getId()
				cli.getXmlRpcClient().execute("ObjektHandler.updateObjekt",id,ObjektArt.MESSUNG_MASSENDATEN.toString(),[it.label,"Automated created by Gateway",it.sampling_interval,null,null,1,2] as Object[])
				db.executeUpdate("update channel set db_series_id = ? where id = ?",id,it.id)
				log.info("Updated db series id for channel: ${it.label}")
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
		Integer bulkSize = config.recordsPerBulk						 
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
					if (row%bulkSize==0){
						log.info("Exporting ${bulkSize} observations")
						cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[]);
						exported += bulkSize
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
