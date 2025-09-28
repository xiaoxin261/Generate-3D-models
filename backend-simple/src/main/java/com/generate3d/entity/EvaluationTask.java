package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评估任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("evaluation_tasks")
public class EvaluationTask {
    
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
     * 模型ID
     */
    private String modelId;
    
    // ========== 任务配置 ==========
    
    /**
     * 评估类型: full, quick, custom
     */
    private String evaluationType;
    
    /**
     * 优先级(1-10)
     */
    private Integer priority;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    // ========== 任务状态 ==========
    
    /**
     * 状态: pending, processing, completed, failed, cancelled
     */
    private String status;
    
    /**
     * 进度百分比(0-100)
     */
    private Integer progress;
    
    /**
     * 当前执行步骤
     */
    private String currentStep;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    // ========== 执行信息 ==========
    
    /**
     * 执行节点ID
     */
    private String workerId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    /**
     * 执行时间(秒)
     */
    private Integer executionTime;
    
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
    
    // ========== 业务方法 ==========
    
    /**
     * 判断任务是否待处理
     */
    public boolean isPending() {
        return "pending".equals(this.status);
    }
    
    /**
     * 判断任务是否处理中
     */
    public boolean isProcessing() {
        return "processing".equals(this.status);
    }
    
    /**
     * 判断任务是否已完成
     */
    public boolean isCompleted() {
        return "completed".equals(this.status);
    }
    
    /**
     * 判断任务是否失败
     */
    public boolean isFailed() {
        return "failed".equals(this.status);
    }
    
    /**
     * 判断任务是否已取消
     */
    public boolean isCancelled() {
        return "cancelled".equals(this.status);
    }
    
    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return isFailed() && (retryCount == null || retryCount < maxRetries);
    }
    
    /**
     * 获取任务状态描述
     */
    public String getStatusDescription() {
        if (status == null) return "未知状态";
        
        switch (status) {
            case "pending": return "等待处理";
            case "processing": return "处理中";
            case "completed": return "已完成";
            case "failed": return "失败";
            case "cancelled": return "已取消";
            default: return "未知状态";
        }
    }
    
    /**
     * 获取优先级描述
     */
    public String getPriorityDescription() {
        if (priority == null) return "普通";
        
        if (priority >= 8) return "紧急";
        else if (priority >= 6) return "高";
        else if (priority >= 4) return "普通";
        else return "低";
    }
    
    /**
     * 获取评估类型描述
     */
    public String getEvaluationTypeDescription() {
        if (evaluationType == null) return "完整评估";
        
        switch (evaluationType) {
            case "full": return "完整评估";
            case "quick": return "快速评估";
            case "custom": return "自定义评估";
            default: return "完整评估";
        }
    }
    
    /**
     * 计算任务执行时长(分钟)
     */
    public Long getExecutionDurationMinutes() {
        if (startedAt == null) return 0L;
        
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMinutes();
    }
}