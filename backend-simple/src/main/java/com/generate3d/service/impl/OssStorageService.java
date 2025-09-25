package com.generate3d.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.generate3d.config.AppStorageProperties;
import com.generate3d.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class OssStorageService implements StorageService {

    private final AppStorageProperties props;
    private OSS oss;

    @PostConstruct
    public void init() {
        if ("oss".equalsIgnoreCase(props.getType())) {
            this.oss = new OSSClientBuilder().build(
                    props.getOss().getEndpoint(),
                    props.getOss().getAccessKeyId(),
                    props.getOss().getAccessKeySecret()
            );
        }
    }

    @Override
    public String putObject(String key, InputStream inputStream, String contentType) {
        String finalKey = normalizeKey(key);
        ObjectMetadata meta = new ObjectMetadata();
        if (contentType != null) meta.setContentType(contentType);
        PutObjectRequest req = new PutObjectRequest(props.getOss().getBucket(), finalKey, inputStream, meta);
        oss.putObject(req);
        return finalKey;
    }

    @Override
    public String generateSignedUrl(String key) {
        String finalKey = normalizeKey(key);
        Date exp = new Date(System.currentTimeMillis() + props.getOss().getSignedUrlExpireSeconds() * 1000L);
        URL url = oss.generatePresignedUrl(props.getOss().getBucket(), finalKey, exp);
        return url.toString();
    }
    
    @Override
    public String uploadFile(String fileName, byte[] fileData) {
        String finalKey = normalizeKey(fileName);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(fileData.length);
        meta.setContentType("application/octet-stream");
        
        java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(fileData);
        PutObjectRequest req = new PutObjectRequest(props.getOss().getBucket(), finalKey, inputStream, meta);
        oss.putObject(req);
        return finalKey;
    }
    
    @Override
    public String generateDownloadUrl(String key) {
        return generateSignedUrl(key);
    }
    
    @Override
    public byte[] downloadFile(String key) {
        String finalKey = normalizeKey(key);
        try {
            com.aliyun.oss.model.OSSObject ossObject = oss.getObject(props.getOss().getBucket(), finalKey);
            return ossObject.getObjectContent().readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败: " + key, e);
        }
    }
    
    @Override
    public boolean deleteFile(String key) {
        String finalKey = normalizeKey(key);
        try {
            oss.deleteObject(props.getOss().getBucket(), finalKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeKey(String key) {
        String base = props.getOss().getBaseDir();
        if (base == null) base = "";
        if (!base.isEmpty() && !base.endsWith("/")) base += "/";
        if (key.startsWith("/")) key = key.substring(1);
        return base + key;
    }
}


