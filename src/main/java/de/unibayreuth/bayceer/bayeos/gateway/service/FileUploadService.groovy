package de.unibayreuth.bayceer.bayeos.gateway.service

import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.PostConstruct
import javax.sql.DataSource
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import de.unibayreuth.bayceer.bayeos.gateway.UserSession
import de.unibayreuth.bayceer.bayeos.gateway.model.Upload
import de.unibayreuth.bayceer.bayeos.gateway.model.User
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository
import de.unibayreuth.bayceer.bayeos.gateway.repo.UploadRepository
import java.nio.file.Paths;

@Service
class FileUploadService {
	
	@Autowired
	DataSource dataSource
	
	@Autowired
	UploadRepository repo
	
	@Autowired
	UserSession userSession
	
	@Autowired
	Path localFilePath

	private Logger log = Logger.getLogger(FileUploadService.class)

	public boolean save(MultipartFile file) throws IOException {
		
		if (file.isEmpty()) {
			log.warn("Ignoring empty file ${file.originalFilename}")
			return false;
		}
		UUID uuid = UUID.randomUUID()
		
		Upload upload = new Upload()
		upload.name = file.originalFilename
		upload.uploadTime = new Date()
		upload.size = file.size
		upload.uuid = uuid
		upload.user = userSession.getUser()
		upload.domain = userSession.getDomain()
						
		String binFile = uuid.toString() + ".bin"	
		file.getInputStream().withStream { stream -> 
			Files.copy(stream, localFilePath.resolve(binFile));
		}	
		repo.save(upload)
		return true;
	}

	public boolean delete(Long id) {
		Upload u = repo.findOne(userSession.getUser(),id);
		if (u!=null) {
			repo.delete(id);
			File f = localFilePath.resolve(u.getUuid().toString() + ".bin").toFile();
			return f.delete();
		} else {
			return false;
		}
	}
	
	
	
	
	
	
}
