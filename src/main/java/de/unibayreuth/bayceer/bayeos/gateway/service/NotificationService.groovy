package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.sql.SQLException

import javax.annotation.PostConstruct
import javax.mail.internet.MimeMessage
import javax.servlet.ServletContext
import javax.sql.DataSource

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.spring4.SpringTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

import de.unibayreuth.bayceer.bayeos.gateway.model.NagiosStatus
import groovy.sql.Sql

@Component
public class NotificationService implements Runnable {

	// Calculates the state for every service with an entry in subscription tables
	// and writes entries in service_state table

	@Autowired
	private DataSource dataSource
	
	@Autowired(required = false)
	private MailSender mailSender
	
	@Value('${NOTIFICATION_HOST:}')				
	private String notification_host; 	

	@Value('${NOTIFICATION:false}')
	private Boolean notification;
	
	@Value('${NOTIFICATION_WAIT_SECS:60}')
	private int waitSecs
	
	@Value('${NOTIFICATION_MAX_SOFT_STATES:4}')
	private int maxSoftStates
	
	@Value('${NOTIFICATION_SENDER:"admin@bayeos.uni-bayreuth.de"}')
	private String sender;

	private Logger log = Logger.getLogger(NotificationService.class)
	
	
	@PostConstruct
	public void start(){		
		if (notification_host.equals("")) {			
			notification_host =  InetAddress.getLocalHost().getCanonicalHostName();		
		} 	
		log.info("Notification Host:" + notification_host);
		if (notification && (mailSender != null)) {			
			new Thread(this).start()
			log.info("Notification service started")
		}		
	}
	
	
	@Override
	public void run() {
		try {			
			Thread.sleep(1000*waitSecs)
			while(true) {
				log.debug("Notification service is running.")				
				['board_group', 'board'].each{ service ->					
					def db = new Sql(dataSource)
					try {						
						def sql
						if (service == "board") {
							sql = "select distinct b.id as id, coalesce(b.name,b.origin) as name from board b join notification s on (b.id = s.board_id)"						
						} else {
							sql = "select distinct b.id as id, b.name as name from ${service} b join notification s on (b.id = s.${service}_id)".toString()
						}	
																				
						db.eachRow(sql){ row ->							
							def now = new Date().toTimestamp()																																				
							def lo = db.firstRow("select * from service_state_log where ${service}_id = ?",[row.id])	
							def s = db.firstRow("""SELECT max(greatest(chk.status_complete, chk.status_valid)) as state FROM 
							channel	JOIN channel_check chk ON channel.id = chk.id
							JOIN board ON board.id = channel.board_id
							LEFT JOIN domain ON domain.id = board.domain_id
							LEFT JOIN board_group  ON board_group.id = board.board_group_id
							WHERE ${service}.id = ? and (chk.status_complete > 0 OR chk.status_valid > 0) AND NOT (board.exclude_from_nagios OR channel.exclude_from_nagios)
							""",[row.id])
							def state = s.state ?: 0														
							if (lo == null) {
								// First check
								db.execute("insert into service_state_log (${service}_id,hard_state) values (?,?);",[row.id,state])
								sendMail(service,row.id,row.name,state);
							} else {
								// Successive check
								if (state == lo.hard_state) {
									if (lo.soft_state_count == 0) {
										db.executeUpdate("update service_state_log set last_check_time=?, soft_state_count=0 where id = ?",[now,lo.id])
									}
								} else {
									if (lo.soft_state_count < maxSoftStates) {
										db.executeUpdate("update service_state_log set last_check_time=?, soft_state_count=? where id = ?",[now,++lo.soft_state_count, lo.id])
									} else {																			
										db.executeUpdate("update service_state_log set last_state_change=?, last_check_time=?, soft_state_count=0, hard_state=? where id = ?",[now,now,state,lo.id])																																																																							
										sendMail(service,row.id,row.name,state);
									}
								}
							}				
						}
					} catch (SQLException e){
							log.error(e.getMessage())
					} finally {
						db.close()
					} 										
				}
				log.debug("Notification service finished.")
				Thread.sleep(1000*waitSecs)
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage())
		}
	}

	
	def getServiceMessage(String service, Long id) {
		def ret = []
		def db = new Sql(dataSource)
		try {			
			def sql = """
			  SELECT domain.id AS domain_id,
				domain.name AS domain_name,
				board_group.id AS board_group_id,
				board_group.name AS board_group_name,
				board.id AS board_id,
				coalesce(board.name,board.origin) as board_name,
				channel.id AS channel_id,
				coalesce(channel.name,channel.nr) AS channel_name,
				get_channel_count(channel.id)*10 AS percentage_complete,
				get_completeness_status(get_channel_count(channel.id)) as status_complete,
				channel.status_valid,
				channel.status_valid_msg
				FROM channel 
				JOIN board ON board.id = channel.board_id
				LEFT JOIN domain ON domain.id = board.domain_id
				LEFT JOIN board_group  ON board_group.id = board.board_group_id
				WHERE ${service}.id = ? and (get_completeness_status(get_channel_count(channel.id)) > 0 OR channel.status_valid > 0) 
				AND NOT (board.exclude_from_nagios OR channel.exclude_from_nagios) 
				ORDER BY domain.name, board_group.name, board.origin, channel.nr"""
							
			def body = new StringBuilder();
			def boardGroupName
			def domainName
			def boardName			
			Integer maxState = 0
			db.rows(sql,[id]).each { it ->
				// Domain
				if (it.domain_name != domainName){
					body.append("<p>Domain ${it.domain_name}</p>\n")
					domainName = it.domain_name
				}
				// Group
				if (it.board_group_name != boardGroupName){					
					body.append("<p>&nbsp;<a href=\"https://${notification_host}/gateway/groups/${it.board_group_id}\">Board Group ${it.board_group_name}</a><p>\n")
					boardGroupName = it.board_group_name
				}
				// Board
				if (it.board_name != boardName){
					body.append("<p>&nbsp;&nbsp;<a href=\"https://${notification_host}/gateway/boards/${it.board_id}\">Board ${it.board_name}</a><p>\n")
					boardName = it.board_name
				}
				// Channel
				body.append("<p>&nbsp;&nbsp;&nbsp;<a href=\"https://${notification_host}/gateway/channels/${it.channel_id}\">Channel ${it.channel_name}</a>: ")	
				
						
				if (it.status_complete>0){
					body.append("Data: ${it.percentage_complete}%")
				}
				if (it.status_valid>0){
					body.append(" Values: ${it.status_valid_msg}")
				}
				// State
				if (it.status_complete>maxState){
					maxState = it.status_complete
				}
				if (it.status_valid>maxState){
					maxState = it.status_valid
				}
				body.append("</p>\n")					 					
			}
			if (maxState == 0) {
				body = "OK"
			} 
			def style = """p {
			  display: block;
			  margin-top: 0em;
			  margin-bottom: 0.5em;
			  margin-left: 0;
			  margin-right: 0;
			}"""
			return ['body':"<html><style>${style}</style><body>${body}</body><html>".toString(),'state':maxState];
		} catch (SQLException e){
			log.error(e.getMessage())
		} finally {
			db.close()
		} 										
	}
	

	def sendMail(String device,Long id, String name,Integer state) {		
		def alert = (state>0)?"PROBLEM":"RECOVERY"
		def stateText = NagiosStatus.get(state).toString()
		def service =  device.replaceAll("_", " ").toUpperCase();
		def subject = "${alert}: ${service}: ${name} is ${stateText}"
		def m = getServiceMessage(device, id)
		
		if (m.state != state) {
			log.warn("State changed while sending.")
			return;
		}
		
		log.debug("Sending messages for ${device}: ${id}")		
		def db = new Sql(dataSource)
		try {			
			db.eachRow("select distinct c.email from notification s join contact c on (c.id = s.contact_id) where s.${device}_id = ?",[id]){ it ->
				MimeMessage msg = mailSender.createMimeMessage();
				MimeMessageHelper mh = new MimeMessageHelper(msg, false, "utf-8");
				mh.setText(m.body, true);
				mh.setTo(it.email)								
				mh.setSubject(subject)																
				mh.setFrom( (sender=="")?"admin@${notification_host}":sender)
				try{
					mailSender.send(msg)
				} catch (MailException ex) {
					log.error(ex.getMessage())
				}
			}
		} catch (SQLException e){
			log.error(e.getMessage())
		} finally {
			db.close()
		}
	}
}
