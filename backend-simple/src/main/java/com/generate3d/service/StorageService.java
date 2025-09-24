package com.generate3d.service;

import java.io.InputStream;

public interface StorageService {
    String putObject(String key, InputStream inputStream, String contentType);
    String generateSignedUrl(String key);
    String uploadFile(String fileName, byte[] fileData);
    String generateDownloadUrl(String key);
    byte[] downloadFile(String key);
    boolean deleteFile(String key);
}


