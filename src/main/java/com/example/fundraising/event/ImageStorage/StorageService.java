package com.example.fundraising.event.ImageStorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    File createDir(String naam);
    void init();

    void store(MultipartFile file, File dir);

    Stream<Path> loadAll(String dirName);

    Path load(String filename);

    Resource loadAsResource(String filename, String dirName);
    void deleteDir(String naam);
    void deleteAll();

}
