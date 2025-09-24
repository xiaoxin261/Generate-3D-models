package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型预览响应DTO
 */
@Data
@Schema(description = "模型预览响应")
public class ModelPreviewResponse {
    
    @Schema(description = "模型ID", example = "model_123456")
    private String modelId;
    
    @Schema(description = "模型名称", example = "可爱的小猫咪")
    private String name;
    
    @Schema(description = "模型描述", example = "一只可爱的小猫咪，坐在草地上")
    private String description;
    
    @Schema(description = "文件路径", example = "models/model_123456.gltf")
    private String filePath;
    
    @Schema(description = "缩略图路径", example = "thumbnails/model_123456.png")
    private String thumbnailPath;
    
    @Schema(description = "文件格式", example = "gltf")
    private String fileFormat;
    
    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;
    
    @Schema(description = "顶点数量", example = "1024")
    private Integer verticesCount;
    
    @Schema(description = "面数量", example = "2048")
    private Integer facesCount;
    
    @Schema(description = "包围盒信息", example = "{\"min\": [-1, -1, -1], \"max\": [1, 1, 1]}")
    private String boundingBox;
    
    @Schema(description = "材质类型", example = "plastic")
    private String materialType;
    
    @Schema(description = "主要颜色", example = "#ff0000")
    private String primaryColor;
    
    @Schema(description = "模型尺寸", example = "medium")
    private String modelSize;
    
    @Schema(description = "是否收藏", example = "false")
    private Boolean favorite;
    
    // 预览相关的额外信息
    @Schema(description = "预览URL", example = "https://example.com/preview/model_123456")
    private String previewUrl;
    
    @Schema(description = "3D查看器配置", example = "{\"camera\": {\"position\": [0, 0, 5]}, \"lighting\": {\"ambient\": 0.4}}")
    private String viewerConfig;
    
    @Schema(description = "支持的交互操作", example = "[\"rotate\", \"zoom\", \"pan\"]")
    private String[] supportedOperations;
    
    @Schema(description = "推荐的视角", example = "{\"front\": {\"position\": [0, 0, 5]}, \"side\": {\"position\": [5, 0, 0]}}")
    private String recommendedViews;
}