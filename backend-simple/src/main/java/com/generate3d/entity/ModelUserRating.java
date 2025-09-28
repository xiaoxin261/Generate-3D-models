package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户评分实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model_user_ratings")
public class ModelUserRating {
    
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
     * 用户ID
     */
    private Long userId;
    
    // ========== 评分数据 ==========
    
    /**
     * 总体评分(1-5)
     */
    private BigDecimal overallRating;
    
    /**
     * 质量评分(1-5)
     */
    private BigDecimal qualityRating;
    
    /**
     * 准确性评分(1-5)
     */
    private BigDecimal accuracyRating;
    
    /**
     * 视觉效果评分(1-5)
     */
    private BigDecimal visualRating;
    
    // ========== 反馈内容 ==========
    
    /**
     * 文字反馈
     */
    private String feedbackText;
    
    /**
     * 反馈标签(JSON格式)
     */
    private String feedbackTags;
    
    // ========== 状态管理 ==========
    
    /**
     * 是否匿名评分
     */
    private Integer isAnonymous;
    
    /**
     * 状态: 1-有效, 0-无效
     */
    private Integer status;
    
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
     * 判断评分是否有效
     */
    public boolean isValid() {
        return status != null && status == 1;
    }
    
    /**
     * 判断是否为匿名评分
     */
    public boolean isAnonymousRating() {
        return isAnonymous != null && isAnonymous == 1;
    }
    
    /**
     * 获取评分星级描述
     */
    public String getRatingDescription() {
        if (overallRating == null) return "未评分";
        
        double rating = overallRating.doubleValue();
        if (rating >= 4.5) return "非常满意";
        else if (rating >= 3.5) return "满意";
        else if (rating >= 2.5) return "一般";
        else if (rating >= 1.5) return "不满意";
        else return "非常不满意";
    }
    
    /**
     * 获取星级显示
     */
    public String getStarDisplay() {
        if (overallRating == null) return "☆☆☆☆☆";
        
        int stars = overallRating.intValue();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < stars) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString();
    }
    
    /**
     * 计算平均评分
     */
    public BigDecimal getAverageRating() {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        
        if (overallRating != null) {
            total = total.add(overallRating);
            count++;
        }
        if (qualityRating != null) {
            total = total.add(qualityRating);
            count++;
        }
        if (accuracyRating != null) {
            total = total.add(accuracyRating);
            count++;
        }
        if (visualRating != null) {
            total = total.add(visualRating);
            count++;
        }
        
        if (count == 0) return BigDecimal.ZERO;
        
        return total.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
    }
}