package com.generate3d.validator;

import com.generate3d.dto.ImageTo3DRequest;
import com.generate3d.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 图片生成3D模型参数验证器
 */
@Component
public class ImageParameterValidator {
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".webp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MIN_IMAGE_SIZE = 256;
    private static final int MAX_IMAGE_SIZE = 4096;
    
    private static final List<String> ALLOWED_STYLES = Arrays.asList(
        "realistic", "cartoon", "abstract", "modern", "classic"
    );
    
    private static final List<String> ALLOWED_QUALITIES = Arrays.asList(
        "low", "medium", "high"
    );
    
    /**
     * 验证图片生成3D模型请求参数
     */
    public void validateRequest(ImageTo3DRequest request) {
        validateImage(request.getImage());
        validateDimensions(request.getLength(), request.getWidth(), request.getHeight());
        validateStyle(request.getStyle());
        validateQuality(request.getQuality());
    }
    
    /**
     * 验证图片文件
     */
    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ValidationException("图片文件不能为空");
        }
        
        // 验证文件大小
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("图片文件大小不能超过10MB");
        }
        
//        // 验证文件类型
//        String contentType = image.getContentType();
//        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
//            throw new ValidationException("不支持的图片格式，仅支持JPG、PNG、WEBP格式");
//        }
        
        // 验证文件扩展名
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null) {
            throw new ValidationException("图片文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("不支持的图片扩展名，仅支持.jpg、.jpeg、.png、.webp");
        }
    }
    
    /**
     * 验证尺寸参数
     */
    private void validateDimensions(Double length, Double width, Double height) {
        if (length == null || length < 0.1 || length > 50.0) {
            throw new ValidationException("长度必须在0.1-50.0米之间");
        }
        
        if (width == null || width < 0.1 || width > 50.0) {
            throw new ValidationException("宽度必须在0.1-50.0米之间");
        }
        
        if (height == null || height < 0.1 || height > 50.0) {
            throw new ValidationException("高度必须在0.1-50.0米之间");
        }
        
        // 验证尺寸比例
        double maxDimension = Math.max(Math.max(length, width), height);
        double minDimension = Math.min(Math.min(length, width), height);
        
        if (maxDimension / minDimension > 20) {
            throw new ValidationException("模型尺寸比例不能超过20:1");
        }
    }
    
    /**
     * 验证风格参数
     */
    private void validateStyle(String style) {
        if (style != null && !ALLOWED_STYLES.contains(style.toLowerCase())) {
            throw new ValidationException("不支持的风格类型，支持的风格: " + String.join(", ", ALLOWED_STYLES));
        }
    }
    
    /**
     * 验证质量参数
     */
    private void validateQuality(String quality) {
        if (quality != null && !ALLOWED_QUALITIES.contains(quality.toLowerCase())) {
            throw new ValidationException("不支持的质量等级，支持的质量: " + String.join(", ", ALLOWED_QUALITIES));
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
    
    /**
     * 根据风格推荐尺寸
     */
    public String getDimensionRecommendation(String style) {
        if (style == null) {
            style = "realistic";
        }
        
        switch (style.toLowerCase()) {
            case "realistic":
                return "推荐尺寸: 长6.0m x 宽4.0m x 高3.0m (适合真实感模型)";
            case "cartoon":
                return "推荐尺寸: 长4.0m x 宽4.0m x 高4.0m (适合卡通风格)";
            case "abstract":
                return "推荐尺寸: 长8.0m x 宽6.0m x 高4.0m (适合抽象艺术)";
            case "modern":
                return "推荐尺寸: 长10.0m x 宽8.0m x 高3.0m (适合现代设计)";
            case "classic":
                return "推荐尺寸: 长6.0m x 宽6.0m x 高6.0m (适合经典风格)";
            default:
                return "推荐尺寸: 长6.0m x 宽4.0m x 高3.0m (默认推荐)";
        }
    }
}