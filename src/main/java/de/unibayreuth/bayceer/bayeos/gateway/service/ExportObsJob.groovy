package de.unibayreuth.bayceer.bayeos.gateway.service

import java.sql.SQLException

import javax.annotation.PostConstruct
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import de.unibayreuth.bayceer.bayeos.objekt.ObjektArt


import de.unibayreuth.bayceer.bayeos.client.Client
import groovy.sql.Sql

import org.apache.log4j.Logger
import org.apache.xmlrpc.XmlRpcException


//TODO IOException prüfen

@Component
@Profile("default")
class ExportObsJob implements Runnable  {

	private Logger log = Logger.getLogger(ExportObsJob.class)

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
	private Sql db


	private def createSeriesForChannel(channel_id) {
		def channel = db.firstRow("select c.*, b.db_folder_id from channel c join board b on c.board_id = b.id where c.id=${channel_id}")
		def sId
		try {
			sId = cli.newNode(channel.db_folder_id, channel.name,ObjektArt.MESSUNG_MASSENDATEN).getId()
			log.info("Created new series for channel ${channel.name}")
		} catch (XmlRpcException e){
			if (createFolderForBoard(channel.board_id)) {
				return createSeriesForChannel(channel.id)
			} else {
				return False
			}
		}
		if (channel.unit_id){
			createReferenceForUnit(sId, channel.unit_id)
		}
		db.execute("update channel set db_series_id = ${sId} where id = ${channel.id}")
		return true
	}

	def createReferenceForUnit(channel_db_series_id, unit_id){
		def unit = db.firstRow("select * from unit where id = ${unit_id}")
		try {
			cli.setNodeReference(unit.db_unit_id,channel_db_series_id,ObjektArt.MESS_EINHEIT)
			log.info("Created reference on unit:${unit.name} for series ${channel_db_series_id}")
		} catch (XmlRpcException e){
			if (createUnitForUnit(unit.name, unit_id)){
				return createReferenceForUnit(channel_db_series_id, unit_id)
			} else {
				return false
			}
		}
		return true
	}

	def createUnitForUnit(unit_name, unit_id) {
		def parentId = expHomeUnitId
		def fId = cli.newNode(parentId, unit_name, ObjektArt.MESS_EINHEIT).getId()
		log.info("Created unit: ${unit_name}")
		db.execute("update unit set db_unit_id = ${fId} where id = ${unit_id}")
		return true
	}



	def createFolderForBoard(board_id) {
		def board = db.firstRow("select b.*, g.db_folder_id as group_folder_id from board b left outer join board_group g on g.id = b.board_group_id where b.id=${board_id}")
		def fId
		try {
			def parentId = board.group_folder_id ?: expHomeFolderId
			fId = cli.newNode(parentId, board.name, ObjektArt.MESSUNG_ORDNER).getId()
			log.info("Created folder for board: ${board.name}")
			def desc = "Created by BayEOS-Gateway (${InetAddress.getLocalHost().getHostName()}), Board (${board.origin})"
			cli.getXmlRpcClient().execute("ObjektHandler.updateObjekt",fId,ObjektArt.MESSUNG_ORDNER.toString(),[board.name, desc.toString(), null, null, null, null, 2] as Object[])			
		} catch (XmlRpcException e){
			db.execute("update board_group set db_folder_id = null where id = ${board.board_group_id}")
			return createFolderForBoard(board.id)
		}
		db.execute("update board set db_folder_id = ${fId} where id = ${board.id}")
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

	private def createNewSeriesForChannels(){
		db.eachRow("select c.id from channel c join board b on b.id = c.board_id where b.db_folder_id is not null and c.name is not null and c.db_series_id is null and (c.db_exclude_auto_export is null or c.db_exclude_auto_export is false)"){ it ->
			createSeriesForChannel(it.id)
		}
	}

	private def createNewFoldersForBoards(){
		db.eachRow("SELECT id from board where db_folder_id is null and db_auto_export and name is not null"){ it ->
			createFolderForBoard(it.id)
		}
	}


	private def exportNewObservations(Long statId) {
		log.info("Exporting observations")
		def bout  = new ByteArrayOutputStream(8*1024)
		def dout = new DataOutputStream(bout)
		def id = 0
		try {
			while (true){
				def row = 0
				Set channelIds = []
				db.eachRow("SELECT o.id, c.id as channel_id, c.db_series_id, o.result_time,o.result_value FROM observation_calc o join channel c on o.channel_id = c.id order by o.id asc limit ${expBulkSize};")
				{
					id = it.id
					channelIds.add([it.channel_id,it.db_series_id])
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
				db.executeUpdate("update export_job_stat set exported = ? where id = ?",row,statId)
				bout.reset()
				db.execute("delete from observation_calc where id <= ?",[id])
			}

		} catch (Exception e){
			log.error(e.getMessage())
		} finally {
			dout.close()
		}
	}

	@PostConstruct
	public void start(){
		new Thread(this).start()
	}

	private String getExportUrL() {
		return expProto + "://" + expHost + ":" + expPort + expContext
	}


	@Override
	public void run() {
		try {
			Thread.sleep(1000*expWaitSecs);
			while(true){
				
				
				def id = null
				try {					
					log.info("ExportObsJob running")
					cli = Client.getInstance()
					db = new Sql(dataSource)
					
					// Stats
					id = db.executeInsert("INSERT INTO export_job_stat (start_time) values (${new Date().toTimestamp()});")[0][0]																									

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
					createNewFoldersForBoards()
					createNewSeriesForChannels()
					exportNewObservations(id)
					
					db.execute "UPDATE export_job_stat set status=0 WHERE id = $id;"
					
				} catch (Exception e){					
					log.error(e.getMessage())
				} finally {
					try{
						cli.close()
					} catch(XmlRpcException e){
						log.error(e.getMessage())
					}
					try{
						db.execute "UPDATE export_job_stat set end_time = ${new Date().toTimestamp()} WHERE id = ${id};"
						db.close()
					} catch(SQLException e){
						log.error(e.getMessage())
					}
					log.info("ExportObsJob finished")
					Thread.sleep(1000*expWaitSecs)
				}
			}

		} catch (InterruptedException e){
			log.error(e.getMessage())
		}


	}





}

