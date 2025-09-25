package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务状态响应DTO
 */
@Data
@Schema(description = "任务状态响应")
public class TaskStatusResponse {
    
    @Schema(description = "任务ID", example = "task_123456")
    private String taskId;
    
    @Schema(description = "任务类型", example = "generation", allowableValues = {"generation", "export"})
    private String taskType;
    
    @Schema(description = "任务状态", example = "processing", allowableValues = {"pending", "processing", "completed", "failed", "cancelled"})
    private String status;
    
    @Schema(description = "进度百分比", example = "75")
    private Integer progress;
    
    @Schema(description = "错误信息", example = "生成失败：输入文本不符合要求")
    private String errorMessage;
    
    @Schema(description = "预计耗时（秒）", example = "30")
    private Integer estimatedTime;
    
    @Schema(description = "实际耗时（秒）", example = "25")
    private Integer actualTime;
    
    @Schema(description = "剩余时间（秒）", example = "5")
    private Integer remainingTime;
    
    @Schema(description = "开始时间", example = "2024-01-01T12:00:00")
    private LocalDateTime startedAt;
    
    @Schema(description = "完成时间", example = "2024-01-01T12:00:30")
    private LocalDateTime completedAt;
    
    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;
    
    // 生成任务特有字段
    @Schema(description = "关联的模型ID（生成任务）", example = "model_123456")
    private String modelId;
    
    @Schema(description = "输入文本（生成任务）", example = "一只可爱的小猫咪")
    private String inputText;
    
    @Schema(description = "生成参数（生成任务）", example = "{\"material\": \"plastic\", \"color\": \"#ff0000\"}")
    private String generationParams;
    
    // 导出任务特有字段
    @Schema(description = "导出ID（导出任务）", example = "export_123456")
    private String exportId;
    
    @Schema(description = "导出格式（导出任务）", example = "gltf")
    private String exportFormat;
    
    @Schema(description = "输出路径（导出任务）", example = "exports/export_123456.zip")
    private String outputPath;
    
    @Schema(description = "文件大小（导出任务）", example = "2048000")
    private Long fileSize;
    
    @Schema(description = "下载链接（导出任务）", example = "https://example.com/download/export_123456.zip")
    private String downloadUrl;
    
    @Schema(description = "过期时间（导出任务）", example = "2024-01-08T12:00:00")
    private LocalDateTime expirationTime;
}