package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import de.unibayreuth.bayceer.bayeos.gateway.model.ImportStatus;
import de.unibayreuth.bayceer.bayeos.gateway.model.Upload;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public class TestFileImportNotification {
	
	private static final String templateBaseFolder = "src/main/resources/templates/";
		
	@Test
	public void testCreateMailBody() throws IOException {
		
		Context c = new Context();
		
		User usr = new User();
		usr.setFirstName("Oliver");
		usr.setLastName("Archner");
		usr.setFullName();
		
		c.setVariable("user", usr);
		
		c.setVariable("host", "bayconf.bayceer.uni-bayreuth.de");
	
		Date now = new Date();
		Timestamp ts = new Timestamp(now.getTime());
		
		List<Upload> uploads = new ArrayList<Upload>(2);
		
		for(int i=0;i<2;i++) {
			Upload u = new Upload();
			u.setUser(usr);
			u.setImportTime(ts);
			u.setUploadTime(ts);
			u.setName("MyFile.dbd");
			u.setSize(13970);
			u.setSizeAsString();
			u.setImportMessage("200 Frames imported");
			u.setImportStatus((i==0)?ImportStatus.FAILED:ImportStatus.OK);
			uploads.add(u);
		}
		
		c.setVariable("uploads", uploads);
		
		FileTemplateResolver r = new FileTemplateResolver();
		r.setPrefix(templateBaseFolder);
		r.setSuffix(".html");
	
		TemplateEngine e = new TemplateEngine();
		e.setTemplateResolver(r);;
		
		Files.write(Paths.get("target","fileImportNotificationEMail.html"), e.process("fileImportNotification",c).getBytes());
	}
}
