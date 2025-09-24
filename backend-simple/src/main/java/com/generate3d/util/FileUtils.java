package com.generate3d.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具类
 */
@Slf4j
public class FileUtils {
    
    /**
     * 支持的3D模型文件格式
     */
    public static final String[] SUPPORTED_3D_FORMATS = {
        "gltf", "glb", "obj", "stl", "ply", "fbx", "dae", "3ds"
    };
    
    /**
     * 支持的图片格式
     */
    public static final String[] SUPPORTED_IMAGE_FORMATS = {
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    };
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 验证是否为支持的3D模型格式
     */
    public static boolean isSupportedModelFormat(String fileName) {
        String extension = getFileExtension(fileName);
        for (String format : SUPPORTED_3D_FORMATS) {
            if (format.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证是否为支持的图片格式
     */
    public static boolean isSupportedImageFormat(String fileName) {
        String extension = getFileExtension(fileName);
        for (String format : SUPPORTED_IMAGE_FORMATS) {
            if (format.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 生成唯一文件名
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        if (StringUtils.hasText(extension)) {
            return uuid + "." + extension;
        }
        return uuid;
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * 计算文件MD5哈希值
     */
    public static String calculateMD5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(data);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("计算MD5失败", e);
            return null;
        }
    }
    
    /**
     * 计算文件MD5哈希值
     */
    public static String calculateMD5(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return calculateMD5(data);
        } catch (IOException e) {
            log.error("读取文件失败: {}", file.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * 创建目录
     */
    public static boolean createDirectories(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            log.error("创建目录失败: {}", dirPath, e);
            return false;
        }
    }
    
    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 复制文件
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            
            // 确保目标目录存在
            Files.createDirectories(target.getParent());
            
            Files.copy(source, target);
            return true;
        } catch (IOException e) {
            log.error("复制文件失败: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }
    
    /**
     * 读取文件内容为字符串
     */
    public static String readFileAsString(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * 写入字符串到文件
     */
    public static boolean writeStringToFile(String content, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
            return true;
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 压缩文件到ZIP
     */
    public static boolean zipFiles(String[] filePaths, String zipPath) {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (!file.exists()) {
                    log.warn("文件不存在，跳过: {}", filePath);
                    continue;
                }
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    
                    zos.closeEntry();
                }
            }
            
            return true;
            
        } catch (IOException e) {
            log.error("压缩文件失败: {}", zipPath, e);
            return false;
        }
    }
    
    /**
     * 解压ZIP文件
     */
    public static boolean unzipFile(String zipPath, String destDir) {
        try (FileInputStream fis = new FileInputStream(zipPath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(destDir, fileName);
                
                // 确保目录存在
                Files.createDirectories(newFile.getParentFile().toPath());
                
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                
                zis.closeEntry();
            }
            
            return true;
            
        } catch (IOException e) {
            log.error("解压文件失败: {}", zipPath, e);
            return false;
        }
    }
    
    /**
     * 获取文件MIME类型
     */
    public static String getMimeType(String fileName) {
        String extension = getFileExtension(fileName);
        
        switch (extension) {
            case "gltf":
                return "model/gltf+json";
            case "glb":
                return "model/gltf-binary";
            case "obj":
                return "model/obj";
            case "stl":
                return "model/stl";
            case "ply":
                return "model/ply";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "zip":
                return "application/zip";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * 验证文件大小是否在限制范围内
     */
    public static boolean isFileSizeValid(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }
    
    /**
     * 清理临时文件
     */
    public static void cleanupTempFiles(String tempDir, long maxAgeMillis) {
        try {
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            
            Files.walk(tempPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        long lastModified = Files.getLastModifiedTime(path).toMillis();
                        return (currentTime - lastModified) > maxAgeMillis;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("删除临时文件: {}", path);
                    } catch (IOException e) {
                        log.warn("删除临时文件失败: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            log.error("清理临时文件失败: {}", tempDir, e);
        }
    }
}