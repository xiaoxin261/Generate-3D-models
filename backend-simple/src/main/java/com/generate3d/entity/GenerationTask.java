package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模型生成任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_generation_task")
public class GenerationTask {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务唯一标识
     */
    private String taskId;
    
    /**
     * 关联模型ID
     */
    private String modelId;
    
    // ========== 任务信息 ==========
    
    /**
     * 输入文本
     */
    private String inputText;
    
    /**
     * 生成参数(JSON格式)
     */
    private String generationParams;
    
    // ========== 任务状态 ==========
    
    /**
     * 任务状态: pending/processing/completed/failed
     */
    private String status;
    
    /**
     * 进度百分比
     */
    private Integer progress;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    // ========== 时间信息 ==========
    
    /**
     * 预估耗时(秒)
     */
    private Integer estimatedTime;
    
    /**
     * 实际耗时(秒)
     */
    private Integer actualTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    // ========== 时间戳 ==========
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}