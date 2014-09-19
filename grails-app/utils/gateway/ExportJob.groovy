package gateway

import groovy.sql.Sql
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Date
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
			cli = Client.getInstance()			
			db = new Sql(dataSource)			
			
			
			try {
				cli.connect(config.url.toString(),config.userName,config.password)
				om = new ObjektNodeModel()
				
				// Units
				Integer dbHomeUnitId = config.dbHomeUnitId
				if (dbHomeUnitId) {
					syncUnits(dbHomeUnitId)
				}
				// Folder
				Integer dbHomeFolderId = config.dbHomeFolderId				
				syncBoards(dbHomeFolderId)
				
				// Data
				exportObs()

			} catch (Exception e){
				log.error(e.getMessage())
			} finally {
				try{cli.close()} catch(XmlRpcException e){}
				try{db.close()} catch(SQLException e){}
				log.info("ExportJob finished")
			}
				Thread.sleep(1000*60* config.sleepInterval)
			} catch (InterruptedException e) {
			break;
		}
		}
	}


	private void syncUnits(Integer dbHomeUnitId) {
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

	private void syncBoards(Integer dbHomeFolderId) {
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

	private void syncChannels(Long boardId, Integer dbFolderId){
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

	private void exportObs() {
		log.info("Exporting observations")
		def ts = new java.sql.Timestamp(new Date().getTime())
		def bout  = new ByteArrayOutputStream(8*1024)
		def dout = new DataOutputStream(bout)
		try {
			
			
			long row = 0			
			def rows = db.firstRow("select max(id) from observation")						
			if (rows!=null){				
				db.eachRow("SELECT * from get_bayeos_obs(${ts})") {
					dout.writeInt(it.db_series_id)
					dout.writeLong(it.result_time.getTime())
					dout.writeFloat(it.result_value)
					row++
					if (row%config.recordsPerBulk==0){
						log.info("Uploaded ${row} observations")
						cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[]);
						bout.reset()
					}
				}
				dout.flush()
				if (row == 0) {
					log.info("Nothing to do.")
					return
				} else {
					log.info("Uploading ${row} observations")
					cli.getXmlRpcClient().execute("MassenTableHandler.upsertByteRows",[bout.toByteArray()] as Object[]);
				}
				log.info("Move records to archive table.")
				db.call("{ call delete_obs(?,?) }",[ts,rows.max])												
			} else {
				log.info("Nothing to do.")
			}
			
						
			
		} catch (Exception e){
			log.error(e.getMessage())
		} finally {
			dout.close()
		}
	}
}
