package de.unibayreuth.bayceer.bayeos.gateway.service


import java.nio.file.Path
import java.sql.SQLException
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.mail.MessagingException
import javax.mail.internet.AddressException
import javax.mail.internet.MimeMessage
import javax.sql.DataSource

import org.apache.commons.codec.binary.Base64
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import org.springframework.data.web.SortDefault
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring4.SpringTemplateEngine

import bayeos.frame.Parser
import de.unibayreuth.bayceer.bayeos.gateway.NotificationConfig
import de.unibayreuth.bayceer.bayeos.gateway.UserSession
import de.unibayreuth.bayceer.bayeos.gateway.model.Upload
import de.unibayreuth.bayceer.bayeos.gateway.reader.BDBReader
import de.unibayreuth.bayceer.bayeos.gateway.repo.UploadRepository
import groovy.sql.Sql

@Component
class FileImportService implements Runnable {

	@Autowired
	SpringTemplateEngine templateEngine

	@Autowired
	Path localFilePath

	@Autowired
	UploadRepository repo

	@Autowired
	FrameService frameService

	@Autowired(required = false)
	private MailSender mailSender

	@Autowired
	private NotificationConfig notificationConfig

	@Autowired
	private DataSource dataSource

	private final static int FRAMES_PER_POST = 1000

	@Value('${LOCAL_FILE_WAIT_SECS:600}')
	private int waitSecs

	@Value('${LOCAL_FILE_EXPIRED_MONTH:3}')
	private int expiredMonth


	private Logger log = Logger.getLogger(FileImportService.class)

	@PostConstruct
	public void start(){
		new Thread(this).start()
	}

	@Override
	public void run() {
		while(true) {
			Thread.sleep(1000*waitSecs)
			log.debug("File import running ...")
			repo.findByImportTimeIsNullOrderByIdAsc().each { u ->
				try {
					switch(u.extension()) {
						case "bdb":
							importBDB(u)
							break
						default:
							log.warn("Unknown file extension:${u.extension()}")
					}
				} catch (IOException e) {
					log.error(e.getMessage())
				}
			}

			GregorianCalendar c = new GregorianCalendar()
			c.add(GregorianCalendar.MONTH, expiredMonth * -1)
			List<Upload> uploads = repo.findExpired(c.getTime())
			uploads.each { u ->
				localFilePath.resolve(u.name).toFile().delete()
				repo.delete(u.id)
			}
			if (uploads.size()>0) {
				log.info("${uploads.size} expired upload files deleted.")
			}


			log.debug("File import finished.")
		}
	}

	private void sendNotification(Upload u) {
		if (u.user.contact?.email && notificationConfig.notification && mailSender != null) {
			try{
				MimeMessage msg = mailSender.createMimeMessage()
				MimeMessageHelper mh = new MimeMessageHelper(msg, false, "utf-8")
				Context c = new Context()
				u.getUser().setFullName();
				u.setSizeAsString();
				c.setVariable("upload",u)
				c.setVariable("host",notificationConfig.getNotification_host())
				mh.setText(templateEngine.process("fileImportNotification",c), true)
				mh.setTo(u.user.contact.email)
				mh.setSubject("BayEOS Gateway File Import")
				mh.setFrom(notificationConfig.getNotification_sender())
				mailSender.send(msg)
			} catch (Exception ex) {
				log.error(ex.getMessage())
			}
		}
	}

	private void importBDB(Upload u) {
		log.info("Importing file: ${u.name}")
		String bin = u.uuid.toString() + ".bin"
		File file = localFilePath.resolve(bin).toFile()
		FileInputStream fin
		try {
			fin = new FileInputStream(file)
			BDBReader r = new BDBReader(fin)
			r.readHeader()
			String origin = r.readOrigin()

			List<String> frames = new ArrayList<>(FRAMES_PER_POST)
			long bytes = 0

			long frameCount = 0
			byte[] data = null
			while ((data = r.readData())!=null){
				bytes += data.length
				frameCount++
				frames.add(Base64.encodeBase64String(data))
				if (frames.size() == FRAMES_PER_POST) {
					if (!frameService.saveFrames(u.domainId,origin,frames)) {
						throw new IOException("Failed to save frames")
					}
					frames.clear()
				}
			}
			if (!frames.isEmpty()) {
				if (!frameService.saveFrames(u.domainId,origin,frames)) {
					throw new IOException("Failed to save frames")
				}
				frames.clear()
			}
			u.importTime = new Date()
			u.importMessage = "${frameCount} frames imported."
			Upload pe = repo.save(u)
			sendNotification(pe)
		} catch (IOException e) {
			log.error(e.getMessage())
		} finally {
			if (fin != null) {
				fin.close()
			}
		}
	}
}
