package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型响应DTO
 */
@Data
@Schema(description = "模型响应")
public class ModelResponse {
    
    @Schema(description = "任务ID", example = "task_123456")
    private String taskId;
    
    @Schema(description = "模型ID", example = "model_123456")
    private String modelId;
    
    @Schema(description = "任务进度", example = "50")
    private Integer progress;
    
    @Schema(description = "预估时间（秒）", example = "25")
    private Integer estimatedTime;
    
    @Schema(description = "模型名称", example = "可爱的小猫咪")
    private String name;
    
    @Schema(description = "模型描述", example = "一只可爱的小猫咪，坐在草地上")
    private String description;
    
    @Schema(description = "原始输入文本", example = "一只可爱的小猫咪，坐在草地上")
    private String originalText;
    
    @Schema(description = "模型分类", example = "animal")
    private String category;
    
    @Schema(description = "文件路径", example = "models/model_123456.gltf")
    private String filePath;
    
    @Schema(description = "缩略图路径", example = "thumbnails/model_123456.png")
    private String thumbnailPath;
    
    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;
    
    @Schema(description = "文件格式", example = "gltf")
    private String fileFormat;
    
    @Schema(description = "顶点数量", example = "1024")
    private Integer verticesCount;
    
    @Schema(description = "面数量", example = "2048")
    private Integer facesCount;
    
    @Schema(description = "包围盒信息", example = "{\"min\": [-1, -1, -1], \"max\": [1, 1, 1]}")
    private String boundingBox;
    
    @Schema(description = "生成参数", example = "{\"material\": \"plastic\", \"color\": \"#ff0000\"}")
    private String generationParams;
    
    @Schema(description = "材质类型", example = "plastic")
    private String materialType;
    
    @Schema(description = "主要颜色", example = "#ff0000")
    private String primaryColor;
    
    @Schema(description = "模型尺寸", example = "medium")
    private String modelSize;
    
    @Schema(description = "模型状态", example = "pending")
    private String status;
    
    @Schema(description = "是否收藏", example = "false")
    private Boolean favorite;
    
    @Schema(description = "生成时间", example = "2024-01-01T12:00:00")
    private LocalDateTime generatedAt;
    
    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
}