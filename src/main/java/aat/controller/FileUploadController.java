package aat.controller;

import aat.exception.StorageFileNotFoundException;
import aat.service.api.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import javax.servlet.http.HttpSession;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    
    @Value("${pdfifier.url}")
    private  String PDFIFIER_URL;
    
    @Value("${merger.allowed.file.upload.types}")
    private  String allowedFileUploadTypes;
    
    @Value("${merger.max.file.size}")
    private  String maxFileSizes;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(value={"/","/merger"})

    public String getUploadFormPage(Model model,HttpSession session) throws IOException {
    	storageService.CreateWorkingFolder(session.getId());
    	model.addAttribute("sessionId",session.getId());
    	model.addAttribute("PDFIFIER_URL",PDFIFIER_URL);
    	model.addAttribute("allowedFileUploadTypes",allowedFileUploadTypes);
    	model.addAttribute("maxFileSizes",maxFileSizes);
    	//model.addAttribute("loadedFiles",storageService.listUploadedFiles());
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    } 

    @PostMapping("/")
    public synchronized String handleFileUpload(@RequestParam("files") MultipartFile[] files,
                                   RedirectAttributes redirectAttributes, Model model) {
    	String errorMessage = "";
    	for (MultipartFile file : files)
    	{
    		try {
				storageService.store(file);
			} catch (FileAlreadyExistsException faee) {
				errorMessage = "Error: Duplicate File(s) Upload!" ;
				 redirectAttributes.addFlashAttribute("errorMessage",errorMessage);
				 // to-do: add a logging tool
				 faee.printStackTrace();
			} catch (IOException e) {
				errorMessage = "Ops! Somthing Went Wrong" ;
			    redirectAttributes.addFlashAttribute("errorMessage",errorMessage);
			 // to-do: add a logging tool
			    e.printStackTrace();
			}
    	}
        
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
