package com.generate3d.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 图片处理服务
 */
@Slf4j
@Service
public class ImageProcessingService {

    // 支持的图片格式
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
        "jpg", "jpeg", "png", "bmp", "gif", "webp"
    );
    
    // 最大图片尺寸（像素）
    private static final int MAX_WIDTH = 4096;
    private static final int MAX_HEIGHT = 4096;
    
    // 最大文件大小（10MB）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 验证图片文件
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("图片文件大小不能超过10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        // 尝试通过实际读取图片内容来验证（最可靠的方法）
        try {
            BufferedImage testImage = ImageIO.read(file.getInputStream());
            if (testImage == null) {
                throw new IllegalArgumentException("文件不是有效的图片格式");
            }
            log.info("图片内容验证通过 - 实际尺寸: {}x{}", testImage.getWidth(), testImage.getHeight());
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取图片文件，请确保文件是有效的图片格式");
        }
        
        // 如果有文件名，检查扩展名（作为辅助验证）
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!SUPPORTED_FORMATS.contains(extension)) {
                log.warn("文件扩展名 '{}' 不在支持列表中，但图片内容验证通过", extension);
            }
        }
        
        log.info("图片文件验证通过 - 文件名: {}, 大小: {} bytes, 类型: {}", 
                originalFilename, file.getSize(), contentType);
    }
    
    /**
     * 预处理图片
     */
    public byte[] preprocessImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }
        
        log.info("原始图片尺寸: {}x{}", originalImage.getWidth(), originalImage.getHeight());
        
        // 调整图片尺寸（如果需要）
        BufferedImage processedImage = resizeImageIfNeeded(originalImage);
        
        // 转换为标准格式（JPEG）
        byte[] processedBytes = convertToStandardFormat(processedImage, "jpg");
        
        log.info("图片预处理完成 - 处理后大小: {} bytes", processedBytes.length);
        return processedBytes;
    }
    
    /**
     * 调整图片尺寸（如果超过最大尺寸）
     */
    private BufferedImage resizeImageIfNeeded(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 如果图片尺寸在允许范围内，直接返回
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return originalImage;
        }
        
        // 计算缩放比例
        double scaleX = (double) MAX_WIDTH / originalWidth;
        double scaleY = (double) MAX_HEIGHT / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        log.info("调整图片尺寸: {}x{} -> {}x{}", originalWidth, originalHeight, newWidth, newHeight);
        
        // 创建缩放后的图片
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * 转换为标准格式
     */
    private byte[] convertToStandardFormat(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 如果是PNG或其他带透明度的格式，转换为RGB
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            image = rgbImage;
        }
        
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
    
    /**
     * 获取图片信息
     */
    public ImageInfo getImageInfo(MultipartFile file) throws IOException {
        validateImageFile(file);
        
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("无法读取图片文件");
        }
        
        ImageInfo info = new ImageInfo();
        info.setWidth(image.getWidth());
        info.setHeight(image.getHeight());
        info.setFormat(getFileExtension(file.getOriginalFilename()));
        info.setFileSize(file.getSize());
        info.setAspectRatio((double) image.getWidth() / image.getHeight());
        
        return info;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private int width;
        private int height;
        private String format;
        private long fileSize;
        private double aspectRatio;
        
        // Getters and Setters
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public double getAspectRatio() { return aspectRatio; }
        public void setAspectRatio(double aspectRatio) { this.aspectRatio = aspectRatio; }
        
        @Override
        public String toString() {
            return String.format("ImageInfo{width=%d, height=%d, format='%s', fileSize=%d, aspectRatio=%.2f}",
                    width, height, format, fileSize, aspectRatio);
        }
    }
}