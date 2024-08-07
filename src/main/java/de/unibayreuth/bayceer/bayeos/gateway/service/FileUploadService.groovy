package de.unibayreuth.bayceer.bayeos.gateway.service

import java.nio.file.Path
import java.nio.file.Files

import javax.persistence.EntityNotFoundException
import javax.sql.DataSource

import org.apache.tomcat.util.http.fileupload.FileUpload
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import de.unibayreuth.bayceer.bayeos.gateway.UserSession
import de.unibayreuth.bayceer.bayeos.gateway.model.Upload
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UploadRepository

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

	private Logger log = LoggerFactory.getLogger(FileUploadService.class)

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
						
			
		file.getInputStream().withStream { stream -> 
			Files.copy(stream, localFilePath.resolve(upload.getLocalFileName()));
		}	
		repo.save(upload)
		return true;
	}

	public boolean delete(Long id) {
		Upload u = repo.getById(id);
		if (u!=null) {
			repo.delete(userSession.getUser(),id);
			File f = localFilePath.resolve(u.getLocalFileName()).toFile();
			return f.delete();
		} else {
			return false;
		}	
	}
	
	
	
	
	
	
}
