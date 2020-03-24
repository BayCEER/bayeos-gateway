package de.unibayreuth.bayceer.bayeos.gateway.service


import java.nio.file.Path

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import org.apache.commons.codec.binary.Base64
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import org.springframework.data.web.SortDefault
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import bayeos.frame.Parser
import de.unibayreuth.bayceer.bayeos.gateway.UserSession
import de.unibayreuth.bayceer.bayeos.gateway.model.Upload
import de.unibayreuth.bayceer.bayeos.gateway.reader.BDBReader
import de.unibayreuth.bayceer.bayeos.gateway.repo.UploadRepository

@Component
class FileImportService implements Runnable {
		
	@Autowired
	Path localFilePath
	
	@Autowired
	UploadRepository repo
	
	@Autowired
	FrameService frameService
	
	private final static int FRAMES_PER_POST = 1000;
	
	@Value('${LOCAL_FILE_WAIT_SECS:60}')
	private int waitSecs
	
	private Logger log = Logger.getLogger(FileImportService.class)

	@PostConstruct
	public void start(){
		new Thread(this).start()
	}
	
	@Override
	public void run() {
		while(true) {
			Thread.sleep(1000*waitSecs);
			log.info("File import running ...")
			repo.findAllByOrderByIdAsc().each { u ->
				try {
				switch(u.extension()) {
					case "bdb":
						importBDB(u)
						break;
					default:
						log.warn("Unknown file extension:${u.extension()}")
				}
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			log.info("File import finished.")
			
		}
	}
	
	private void importBDB(Upload u) {
		
		log.info("Importing ${u.name}")
		String bin = u.uuid.toString() + ".bin";
		File file = localFilePath.resolve(bin).toFile();
		FileInputStream fin;
		try {
			fin = new FileInputStream(file)
			BDBReader r = new BDBReader(fin)
			r.readHeader()
			String origin = r.readOrigin()
			
			List<String> frames = new ArrayList<>(FRAMES_PER_POST);
			long bytes = 0;
			
			byte[] data = null
			while ((data = r.readData())!=null){
				bytes += data.length;
				frames.add(Base64.encodeBase64String(data))
				if (frames.size() == FRAMES_PER_POST) {
					if (!frameService.saveFrames(u.domainId,origin,frames)) {
						throw new IOException("Failed to save frames");
					}
					frames.clear();
				}
			}
			if (!frames.isEmpty()) {
				if (!frameService.saveFrames(u.domainId,origin,frames)) {
					throw new IOException("Failed to save frames");
				}
				frames.clear();
			}
			repo.delete(u.getId());
			file.delete()
	
		} catch (IOException e) {
			log.error(e.getMessage())
		} finally {
			if (fin != null) {
				fin.close()
			}
			
		}
		
	}
		
			
			
}
