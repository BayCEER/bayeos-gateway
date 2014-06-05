package gateway

import groovy.sql.Sql
import java.sql.SQLException

import de.unibayreuth.bayceer.bayeos.client.Client

class DeleteJob implements Runnable {

	private dataSource

	private DeleteJobConfig config

	public DeleteJob(dataSource, DeleteJobConfig config){
		this.dataSource = dataSource
		this.config = config
	}

	@Override
	public void run() {
		while(true) {
			try {
			log.info("DeleteJob running")			
			def db = new Sql(dataSource)
			try {
				String dt = config.maxResultInterval.toString()
				log.info("Deleting observations older than ${dt}.")
				db.execute("delete from observation where insert_time < now() - ?::interval",[dt])
				log.info("Deleting exorted observations older than ${dt}.")
				db.execute("delete from observation_exp where insert_time < now() - ?::interval",[dt])				
				dt = config.maxMessageInterval.toString()
				log.info("Deleting messages older than ${dt}.")
				db.execute("delete from message where insert_time < now() - ?::interval",[dt])				
				// Achtung: Matrix Boards Daten in _board_data Tabellen werden nicht gelöscht.
			} catch (SQLException e){
				log.error(e)
			} finally {
				db.close()
				log.info("DeleteJob finished")
			}		
			Thread.sleep(1000*60* config.delayInterval)
			} catch (InterruptedException e) {
				break;
			}
			
		}
	}
}

