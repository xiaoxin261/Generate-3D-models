package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型评估结果实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_evaluations")
public class ModelEvaluation {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模型ID
     */
    private String modelId;
    
    /**
     * 评估算法版本
     */
    private String evaluationVersion;
    
    // ========== 评估分数 ==========
    
    /**
     * 几何质量评分(0-100)
     */
    private BigDecimal geometricScore;
    
    /**
     * 视觉效果评分(0-100)
     */
    private BigDecimal visualScore;
    
    /**
     * 技术指标评分(0-100)
     */
    private BigDecimal technicalScore;
    
    /**
     * 最终综合评分(0-100)
     */
    private BigDecimal finalScore;
    
    /**
     * 评分等级(A+, A, B, C, D)
     */
    private String grade;
    
    // ========== 详细评估数据 ==========
    
    /**
     * 几何质量详细数据(JSON格式)
     */
    private String geometricDetails;
    
    /**
     * 视觉效果详细数据(JSON格式)
     */
    private String visualDetails;
    
    /**
     * 技术指标详细数据(JSON格式)
     */
    private String technicalDetails;
    
    // ========== 评估状态 ==========
    
    /**
     * 评估状态: pending, processing, completed, failed
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 评估耗时(秒)
     */
    private Integer evaluationTime;
    
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
     * 判断评估是否完成
     */
    public boolean isCompleted() {
        return "completed".equals(this.status);
    }
    
    /**
     * 判断评估是否失败
     */
    public boolean isFailed() {
        return "failed".equals(this.status);
    }
    
    /**
     * 判断评估是否进行中
     */
    public boolean isProcessing() {
        return "processing".equals(this.status);
    }
    
    /**
     * 获取评分等级描述
     */
    public String getGradeDescription() {
        if (grade == null) return "未评估";
        
        switch (grade) {
            case "A+": return "优秀+";
            case "A": return "优秀";
            case "B": return "良好";
            case "C": return "一般";
            case "D": return "较差";
            default: return "未知";
        }
    }
    
    /**
     * 获取综合评估描述
     */
    public String getEvaluationSummary() {
        if (finalScore == null) return "评估中...";
        
        return String.format("综合评分: %.1f分 (%s)", 
            finalScore.doubleValue(), getGradeDescription());
    }
}