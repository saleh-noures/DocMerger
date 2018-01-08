package aat.controller;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import aat.common.FileData;
import aat.common.MergeRequest;
import aat.service.api.StorageService;

@RestController
//@Scope("request")
public class FileUploadRestController {
	
    private final StorageService storageService;

    @Autowired
    public FileUploadRestController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    
    /* Returns Files Names to be listed in the Page */
    @GetMapping("/getFiles")
    public List<String> listUploadedFiles() throws IOException {

    	List<String> files =  storageService
                .loadAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList());

        return files;
    }
    
    /* Returns Files Name, Size and type to be sent to the PDFifier */
    @GetMapping("/getFilesData")
    public List<FileData> getFilesData() throws IOException {
        return storageService.getFilesData();
    }
    
    /* Will Clear the Page and delete all files in the Merger working folder */
    @DeleteMapping("/clear")
    public synchronized void deleteUploadedFiles() throws IOException {
    	storageService.deleteAll();
    	//storageService.init();
    }
    
/*    @PostMapping("/merge")
    public ResponseEntity<Resource> MergeUploadedFiles(@RequestBody MergeRequest mergeRequest) {
    	return ResponseEntity.ok().build();
    }*/

}
