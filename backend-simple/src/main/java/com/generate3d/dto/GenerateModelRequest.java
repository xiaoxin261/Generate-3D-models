package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 3D模型生成请求DTO
 */
@Data
@Schema(description = "3D模型生成请求")
public class GenerateModelRequest {
    
    @NotBlank(message = "输入文本不能为空")
    @Size(min = 10, max = 500, message = "输入文本长度必须在10-500字符之间")
    @Schema(description = "输入文本描述", example = "一只可爱的小猫咪，坐在草地上")
    private String text;
    
    @Schema(description = "材质类型", example = "plastic", allowableValues = {"plastic", "metal", "wood", "ceramic", "glass"})
    private String material = "plastic";
    
    @Schema(description = "主要颜色", example = "#ff0000")
    private String color = "#ff0000";
    
    @Schema(description = "模型尺寸", example = "medium", allowableValues = {"small", "medium", "large"})
    private String size = "medium";
    
    @Schema(description = "生成风格", example = "realistic", allowableValues = {"realistic", "cartoon", "abstract", "minimalist"})
    private String style = "realistic";
    
    @Schema(description = "细节级别", example = "medium", allowableValues = {"low", "medium", "high"})
    private String detailLevel = "medium";
}