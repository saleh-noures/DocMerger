package aat.service.api;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import aat.common.FileData;
import aat.storage.StorageProperties;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    void init();
    
    void CreateWorkingFolder(String FolderName);

    void store(MultipartFile file) throws IOException, FileAlreadyExistsException;

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();
    
    List<FileData> getFilesData();

}
