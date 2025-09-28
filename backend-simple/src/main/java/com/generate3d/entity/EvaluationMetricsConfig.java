package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估指标配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("evaluation_metrics_config")
public class EvaluationMetricsConfig {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 指标分类: geometric, visual, technical
     */
    private String metricCategory;
    
    /**
     * 权重(0-1)
     */
    private BigDecimal weight;
    
    /**
     * 优秀阈值
     */
    private BigDecimal thresholdExcellent;
    
    /**
     * 良好阈值
     */
    private BigDecimal thresholdGood;
    
    /**
     * 一般阈值
     */
    private BigDecimal thresholdFair;
    
    /**
     * 是否启用
     */
    private Integer isEnabled;
    
    /**
     * 指标描述
     */
    private String description;
    
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
     * 判断指标是否启用
     */
    public boolean isActive() {
        return isEnabled != null && isEnabled == 1;
    }
    
    /**
     * 获取指标分类描述
     */
    public String getCategoryDescription() {
        if (metricCategory == null) return "未知分类";
        
        switch (metricCategory) {
            case "geometric": return "几何质量";
            case "visual": return "视觉效果";
            case "technical": return "技术指标";
            default: return "未知分类";
        }
    }
    
    /**
     * 根据分数获取等级
     */
    public String getGradeByScore(BigDecimal score) {
        if (score == null) return "未评估";
        
        if (thresholdExcellent != null && score.compareTo(thresholdExcellent) >= 0) {
            return "优秀";
        } else if (thresholdGood != null && score.compareTo(thresholdGood) >= 0) {
            return "良好";
        } else if (thresholdFair != null && score.compareTo(thresholdFair) >= 0) {
            return "一般";
        } else {
            return "较差";
        }
    }
    
    /**
     * 获取权重百分比显示
     */
    public String getWeightPercentage() {
        if (weight == null) return "0%";
        
        return String.format("%.1f%%", weight.multiply(BigDecimal.valueOf(100)).doubleValue());
    }
    
    /**
     * 验证阈值设置是否合理
     */
    public boolean isThresholdValid() {
        if (thresholdExcellent == null || thresholdGood == null || thresholdFair == null) {
            return false;
        }
        
        // 优秀 >= 良好 >= 一般
        return thresholdExcellent.compareTo(thresholdGood) >= 0 
            && thresholdGood.compareTo(thresholdFair) >= 0;
    }
    
    /**
     * 获取指标权重等级
     */
    public String getWeightLevel() {
        if (weight == null) return "无权重";
        
        double w = weight.doubleValue();
        if (w >= 0.2) return "高权重";
        else if (w >= 0.1) return "中权重";
        else if (w > 0) return "低权重";
        else return "无权重";
    }
}