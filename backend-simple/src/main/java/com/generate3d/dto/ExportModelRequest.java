package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 模型导出请求DTO
 */
@Data
@Schema(description = "模型导出请求")
public class ExportModelRequest {
    
    @NotEmpty(message = "模型ID列表不能为空")
    @Schema(description = "模型ID列表", example = "[\"model_123\", \"model_456\"]")
    private List<String> modelIds;
    
    @NotBlank(message = "导出格式不能为空")
    @Schema(description = "导出格式", example = "gltf", allowableValues = {"obj", "stl", "gltf", "ply"})
    private String format;
    
    @Schema(description = "是否包含纹理", example = "true")
    private Boolean includeTextures = true;
    
    @Schema(description = "是否包含材质", example = "true")
    private Boolean includeMaterials = true;
    
    @Schema(description = "压缩级别", example = "medium", allowableValues = {"none", "low", "medium", "high"})
    private String compressionLevel = "medium";
    
    @Schema(description = "导出质量", example = "high", allowableValues = {"low", "medium", "high"})
    private String quality = "high";
}