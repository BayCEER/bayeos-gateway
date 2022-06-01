package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.Upload;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.UploadRepository;
import de.unibayreuth.bayceer.bayeos.gateway.service.FileUploadService;

@Controller
public class UploadController extends AbstractController {
	
	@Autowired 
	private FileUploadService service;

	@Autowired 
	private UploadRepository repo;
	
	@Autowired
	Path localFilePath;
	
	@Value("${LOCAL_FILE_EXPIRED_MONTH:3}")
	private int localFileExpiredMonth;


	private final static Logger log = LoggerFactory.getLogger(UploadController.class);
	
	@PostMapping("/uploads")
	public String upload(@RequestParam("files") MultipartFile[] files, RedirectAttributes redirect) {
		try {
			int n = 0;
			for (MultipartFile file:files) {
				if (service.save(file)) n++;
			}
			redirect.addFlashAttribute("globalMessage", n + " files saved.");
		} catch (IOException e) {
			log.error(e.getMessage());
			redirect.addFlashAttribute("globalMessage", "Failed to save files.");
						
		}													
		return "redirect:/uploads";
	}
	
	@GetMapping("/uploads")
	public String list(Model model, @SortDefault(value = "uploadTime", direction = Direction.DESC) Pageable pageable){
		Page<Upload> uploads = repo.findAll(userSession.getUser(),domainFilter,pageable);
		for (Upload d:uploads) {
			d.setSizeAsString();
			d.getUser().setFullName();
		}
		model.addAttribute("uploads", uploads);
		model.addAttribute("localFileExpiredMonth", localFileExpiredMonth);
		return "listUpload";
	}
	
	@GetMapping("/uploads/uploadFile")
	public String upload(){
		return "uploadFile";
	}
	
	@GetMapping("/uploads/deleteFile/{id}")
	public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
		if (service.delete(id)) {
			redirect.addFlashAttribute("globalMessage", getActionMsg("deleted", locale));	
		} else {
			redirect.addFlashAttribute("globalMessage", "Failed to delete upload file.");
		}
		return "redirect:/uploads";
	}
	
	@GetMapping("/uploads/exportFile/{id}")
	public void export(@PathVariable Long id, HttpServletResponse response ) throws IOException{														
		Upload u =  repo.findOne(userSession.getUser(),id);								
		if (u == null) throw new IOException("Failed to read upload file.");	
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + u.getName() + "\""));
		response.setContentLength((int) u.getSize());
		try(InputStream inputStream = new BufferedInputStream(new FileInputStream(localFilePath.resolve(u.getUuid().toString() + ".bin").toFile()))){
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		}
	}
}
