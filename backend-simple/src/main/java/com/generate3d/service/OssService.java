package com.generate3d.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.generate3d.config.AppStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * OSS服务类
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "oss")
public class OssService {

    private final OSS ossClient;
    private final AppStorageProperties storageProperties;

    public OssService(OSS ossClient, AppStorageProperties storageProperties) {
        this.ossClient = ossClient;
        this.storageProperties = storageProperties;
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param folder 文件夹路径
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            
            // 生成文件名：日期/UUID.扩展名
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = dateStr + "/" + UUID.randomUUID().toString() + extension;
            
            // 完整的对象键
            String objectKey = storageProperties.getOss().getBaseDir() + folder + "/" + fileName;
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    storageProperties.getOss().getBucket(),
                    objectKey,
                    file.getInputStream()
            );
            
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            
            log.info("文件上传成功 - objectKey: {}, eTag: {}", objectKey, result.getETag());
            
            return getFileUrl(objectKey);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件流
     *
     * @param inputStream 输入流
     * @param fileName 文件名
     * @param folder 文件夹路径
     * @return 文件访问URL
     */
    public String uploadFile(InputStream inputStream, String fileName, String folder) {
        try {
            // 生成文件名：日期/文件名
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectKey = storageProperties.getOss().getBaseDir() + folder + "/" + dateStr + "/" + fileName;
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    storageProperties.getOss().getBucket(),
                    objectKey,
                    inputStream
            );
            
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            
            log.info("文件上传成功 - objectKey: {}, eTag: {}", objectKey, result.getETag());
            
            return getFileUrl(objectKey);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectKey 对象键
     */
    public void deleteFile(String objectKey) {
        try {
            ossClient.deleteObject(storageProperties.getOss().getBucket(), objectKey);
            log.info("文件删除成功 - objectKey: {}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败 - objectKey: {}", objectKey, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL
     *
     * @param objectKey 对象键
     * @return 访问URL
     */
    public String getFileUrl(String objectKey) {
        try {
            // 生成签名URL，有效期为配置的时间
            Date expiration = new Date(System.currentTimeMillis() + 
                    storageProperties.getOss().getSignedUrlExpireSeconds() * 1000L);
            
            URL url = ossClient.generatePresignedUrl(
                    storageProperties.getOss().getBucket(),
                    objectKey,
                    expiration
            );
            
            return url.toString();
        } catch (Exception e) {
            log.error("生成文件访问URL失败 - objectKey: {}", objectKey, e);
            throw new RuntimeException("生成文件访问URL失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param objectKey 对象键
     * @return 是否存在
     */
    public boolean doesObjectExist(String objectKey) {
        try {
            return ossClient.doesObjectExist(storageProperties.getOss().getBucket(), objectKey);
        } catch (Exception e) {
            log.error("检查文件是否存在失败 - objectKey: {}", objectKey, e);
            return false;
        }
    }

    /**
     * 获取文件信息
     *
     * @param objectKey 对象键
     * @return 文件信息
     */
    public ObjectMetadata getObjectMetadata(String objectKey) {
        try {
            return ossClient.getObjectMetadata(storageProperties.getOss().getBucket(), objectKey);
        } catch (Exception e) {
            log.error("获取文件信息失败 - objectKey: {}", objectKey, e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 从URL中提取对象键
     *
     * @param url 文件URL
     * @return 对象键
     */
    public String extractObjectKeyFromUrl(String url) {
        try {
            // 从URL中提取对象键
            String bucketName = storageProperties.getOss().getBucket();
            String endpoint = storageProperties.getOss().getEndpoint();
            
            // URL格式：https://bucket.endpoint/objectKey?签名参数
            String prefix = "https://" + bucketName + "." + endpoint + "/";
            if (url.startsWith(prefix)) {
                String objectKey = url.substring(prefix.length());
                // 移除查询参数
                int queryIndex = objectKey.indexOf('?');
                if (queryIndex > 0) {
                    objectKey = objectKey.substring(0, queryIndex);
                }
                return objectKey;
            }
            
            return null;
        } catch (Exception e) {
            log.error("从URL提取对象键失败 - url: {}", url, e);
            return null;
        }
    }
}