package de.unibayreuth.bayceer.bayeos.gateway.service


import java.nio.file.Path
import java.sql.Timestamp
import javax.annotation.PostConstruct
import javax.mail.internet.MimeMessage
import javax.persistence.EntityNotFoundException
import javax.sql.DataSource
import java.util.Base64
import de.unibayreuth.bayceer.bayeos.gateway.model.ImportStatus

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine

import de.unibayreuth.bayceer.bayeos.gateway.NotificationConfig
import de.unibayreuth.bayceer.bayeos.gateway.model.Upload
import de.unibayreuth.bayceer.bayeos.gateway.model.User
import de.unibayreuth.bayceer.bayeos.gateway.reader.BDBReader
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UploadRepository
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UserRepository

@Component
class FileImportService implements Runnable {

	@Autowired
	SpringTemplateEngine templateEngine

	@Autowired
	Path localFilePath

	@Autowired
	UploadRepository repo
	
	@Autowired
	UserRepository repoUser

	@Autowired
	FrameService frameService
    
    @Autowired
    FileUploadService uploadService

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


	private Logger log = LoggerFactory.getLogger(FileImportService.class)

	@PostConstruct
	public void start(){
		new Thread(this).start()
	}

	@Override
	public void run() {
		while(true) {
			Thread.sleep(1000*waitSecs)
			log.info("File import running ...")
			
			// Import and save results 
			Map<Integer,List<Upload>> umap = new HashMap();
			repo.findPending().each{ u ->			
				if (umap[u.user.id] == null) {
					umap[u.user.id] = new ArrayList<Upload>()
				}
				Upload e = repo.save(importFile(u))
				umap[u.user.id].add(e)	 
			}
			
			// Send email for each user
			umap.each { id, files -> 
				sendNotification(id, files)
			}
			
			// Delete imported 
			GregorianCalendar c = new GregorianCalendar()
			c.add(GregorianCalendar.MONTH, expiredMonth * -1)
			List<Upload> uploads = repo.findExpired(c.getTime())
			uploads.each { u ->
			    localFilePath.resolve(u.getLocalFileName()).toFile().delete()
			    repo.deleteById(u.id)
                log.info("File ${u.uuid}.bin deleted.")                                 
			}			
			log.info("File import finished")
		}
	}

	private void sendNotification(Long id, List<Upload> uploads) {
       Optional<User> usrOptional = repoUser.findById(id)                
       if (usrOptional.isPresent()) {
            User usr = usrOptional.get()           
            if (usr?.hasEmail() && notificationConfig.notification && mailSender != null) {
                try{
                    log.info("Sending import notification to: " + usr.contact.email)
                    MimeMessage msg = mailSender.createMimeMessage()
                    MimeMessageHelper mh = new MimeMessageHelper(msg, false, "utf-8")
                    Context c = new Context()
                    usr.setFullName()
                    c.setVariable("user", usr)
                    uploads.each { ui ->
                        ui.setSizeAsString()
                    }
                    c.setVariable("uploads",uploads)
                    c.setVariable("host",notificationConfig.getNotification_host())
                    mh.setText(templateEngine.process("fileImportNotification",c), true)
                    mh.setTo(usr.contact.email)
                    mh.setSubject("BayEOS Gateway File Import")
                    mh.setFrom(notificationConfig.getNotification_sender())
                    mailSender.send(msg)
                } catch (Exception ex) {
                    log.error(ex.getMessage())
                }
            }                        
        } else {
           log.warn("User:"+id+" not found.") 
        }		
	}
	
	private Upload importFile(Upload u) {
		log.info("Importing file: ${u.name}")
		
		String ext = u.extension()		
		File file = localFilePath.resolve(u.getLocalFileName()).toFile()
		FileInputStream fin
		try {
			fin = new FileInputStream(file)		
			switch (ext) {
				case "bdb":
					BDBReader r = new BDBReader(fin)
					r.readHeader()
					String origin = r.readOrigin()
					List<String> frames = new ArrayList<>(FRAMES_PER_POST)
					long frameCount = 0
					byte[] data = null
					while ((data = r.readData())!=null){
						frameCount++
						frames.add(Base64.encoder.encodeToString(data))
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
					Date now = new Date()
					u.setImportMessage("${frameCount} frames imported.")
					u.setImportStatus(ImportStatus.OK);
					break;
				default:
					throw new IOException("Unknown file type:${ext}")
			}
				
		} catch (IOException e) {
				log.error(e.getMessage())
				u.setImportMessage(e.getMessage())
				u.setImportStatus(ImportStatus.FAILED)
		} finally {
				if (fin != null) {
					fin.close()
				}
		}
		Date now = new Date()
		u.importTime = new Timestamp(now.time)
		return u;
	}


}
