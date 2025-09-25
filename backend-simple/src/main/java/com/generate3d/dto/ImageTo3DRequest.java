package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

/**
 * 图片生成3D模型请求DTO
 */
@Data
@Schema(description = "图片生成3D模型请求")
public class ImageTo3DRequest {
    
    @NotNull(message = "图片文件不能为空")
    @Schema(description = "上传的图片文件", required = true)
    private MultipartFile image;
    
    @DecimalMin(value = "0.1", message = "长度必须大于0.1米")
    @DecimalMax(value = "50.0", message = "长度不能超过50米")
    @Schema(description = "长度(m)", example = "6.0", minimum = "0.1", maximum = "50.0")
    private Double length = 6.0;
    
    @DecimalMin(value = "0.1", message = "宽度必须大于0.1米")
    @DecimalMax(value = "50.0", message = "宽度不能超过50米")
    @Schema(description = "宽度(m)", example = "4.0", minimum = "0.1", maximum = "50.0")
    private Double width = 4.0;
    
    @DecimalMin(value = "0.1", message = "高度必须大于0.1米")
    @DecimalMax(value = "50.0", message = "高度不能超过50米")
    @Schema(description = "高度(m)", example = "3.0", minimum = "0.1", maximum = "50.0")
    private Double height = 3.0;
    
    @Schema(description = "风格类型", example = "realistic", 
            allowableValues = {"realistic", "cartoon", "abstract", "modern", "classic"})
    private String style = "realistic";
    
    @Schema(description = "生成质量", example = "medium", 
            allowableValues = {"low", "medium", "high"})
    private String quality = "medium";
    
    @Schema(description = "是否保留图片细节", example = "true")
    private Boolean preserveDetails = true;
}