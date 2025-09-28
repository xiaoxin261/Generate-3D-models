package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评估历史记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("evaluation_history")
public class EvaluationHistory {
    
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
     * 评估结果ID
     */
    private Long evaluationId;
    
    /**
     * 评估版本号
     */
    private Integer version;
    
    // ========== 评估快照 ==========
    
    /**
     * 评分快照(JSON格式)
     */
    private String scoreSnapshot;
    
    /**
     * 配置快照(JSON格式)
     */
    private String configSnapshot;
    
    // ========== 变更信息 ==========
    
    /**
     * 变更原因
     */
    private String changeReason;
    
    /**
     * 变更人ID
     */
    private Long changedBy;
    
    // ========== 时间戳 ==========
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // ========== 业务方法 ==========
    
    /**
     * 获取变更原因描述
     */
    public String getChangeReasonDescription() {
        if (changeReason == null) return "未知原因";
        
        switch (changeReason) {
            case "Auto update": return "自动更新";
            case "Manual update": return "手动更新";
            case "Algorithm upgrade": return "算法升级";
            case "Config change": return "配置变更";
            case "Re-evaluation": return "重新评估";
            default: return changeReason;
        }
    }
    
    /**
     * 判断是否为自动更新
     */
    public boolean isAutoUpdate() {
        return "Auto update".equals(changeReason);
    }
    
    /**
     * 判断是否为手动更新
     */
    public boolean isManualUpdate() {
        return "Manual update".equals(changeReason);
    }
    
    /**
     * 获取版本显示
     */
    public String getVersionDisplay() {
        return version != null ? "v" + version : "v1";
    }
}