package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import de.unibayreuth.bayceer.bayeos.gateway.model.Upload;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public class TestFileImportNotification {
	
	private static final String templateBaseFolder = "src/main/resources/templates/";
		
	@Test
	public void testCreateMailBody() throws IOException {
		
		Context c = new Context();
		Upload u = new Upload();
		User usr = new User();
		usr.setFirstName("Oliver");
		usr.setLastName("Archner");
		usr.setFullName();
		u.setUser(usr);
		u.setImportTime(new Date());
		u.setUploadTime(new Date());
		u.setName("MyFile.dbd");
		u.setSize(13970);
		u.setSizeAsString();
		u.setImportMessage("200 Frames imported");
		c.setVariable("upload", u);
		c.setVariable("host", "bayconf.bayceer.uni-bayreuth.de");
		
		TemplateResolver r = new FileTemplateResolver();
		r.setPrefix(templateBaseFolder);
		r.setSuffix(".html");
	
		TemplateEngine e = new TemplateEngine();
		e.setTemplateResolver(r);;
		e.initialize();
		
		Files.write(Paths.get("target","tests","fileImportNotificationEMail.html"), e.process("fileImportNotification",c).getBytes());
	}
}
