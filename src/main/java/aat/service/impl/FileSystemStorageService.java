package aat.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import aat.common.FileData;
import aat.exception.StorageException;
import aat.exception.StorageFileNotFoundException;
import aat.service.api.StorageService;
import aat.storage.StorageProperties;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service

/*
 * Placing service class is the session scope is not a best practice 
 * but this application is still in the POC phase so when possible it should 
 * be back to singleton (the default)
*/
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class FileSystemStorageService implements StorageService {

    
	StorageProperties properties;
    
	/* Points to the parent folder where the working folders will be created */
    private final Path rootLocation;
    
    /* Points to the working folders which take their name form the session IDs */
    private Path WorkingLocation;
    
    /* PDFifier (.net) has a limitation of the file name size*/
    private final int MAX_FILE_NAME_SIZE = 165;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
    	this.properties = properties;
        this.rootLocation = Paths.get(this.properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) throws IOException, FileAlreadyExistsException  {
        try {
        	
                if (file.isEmpty())
                {
                    throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
                }
                
                /*
                 * Chrome and FireFox send the file name only which we really need 
                 * while IE sends the file path name of the file so wee need to extract 
                 * the file name from the full path.
                 */
                String fileName;
                if (file.getOriginalFilename().indexOf("\\") == -1)
                {
              	    fileName = file.getOriginalFilename();
                }
                else
                {
            	    fileName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("\\") + 1);
                }
            
                if (fileName.length() > MAX_FILE_NAME_SIZE)
                {
                	String fileExtention = fileName.substring(fileName.lastIndexOf("."));
                	String NewfileNameNoExt = fileName.substring(0,MAX_FILE_NAME_SIZE);
                	fileName = NewfileNameNoExt + "..." + fileExtention;
                }
            Files.copy(file.getInputStream(), this.WorkingLocation.resolve(fileName));
            
        }
         catch (FileAlreadyExistsException faee) {
            throw new FileAlreadyExistsException(null);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
	            return Files.walk(this.WorkingLocation, 1)
	                    .filter( path -> !path.equals(this.WorkingLocation))
	                    .filter( path -> !path.endsWith("merged.pdf"))
	                    .map(path -> this.WorkingLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return WorkingLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
    try{
         Files.walk(this.WorkingLocation, 1)
                .filter( path -> !path.equals(this.WorkingLocation))
                .forEach(path -> FileSystemUtils.deleteRecursively(path.toFile()));
    } catch (IOException e){
        throw new StorageException("Failed to read stored files", e);
    }

    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
    

    public void CreateWorkingFolder(String sessionId){
        try {
        	properties.setWorkingFolder(properties.getLocation() +"\\" + sessionId);
        	this.WorkingLocation = Paths.get(properties.getWorkingFolder());
        	if (!Files.exists(this.WorkingLocation))
        	{
                Files.createDirectory(this.WorkingLocation);
        	}
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
    
    public List<FileData> getFilesData()
    {
    	List<FileData> filesData  = new ArrayList<>();
    	
    	try {
			Files.walk(this.WorkingLocation, 1)
			.filter( path -> !path.equals(this.WorkingLocation))
			.forEach(path->{ 
				FileData fileData = FileData.create(FileData::new);
				File file = path.toFile();
				fileData.setFileSize(file.length());
				fileData.setName(file.getName());
				fileData.setType(file.getName().substring(file.getName().lastIndexOf(".")+1));
				filesData.add(fileData);
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
        return filesData;
    }

}
