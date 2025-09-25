package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 导出任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_export_task")
public class ExportTask {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 导出任务唯一标识
     */
    private String exportId;
    
    // ========== 导出信息 ==========
    
    /**
     * 模型ID列表(JSON格式)
     */
    private String modelIds;
    
    /**
     * 导出格式
     */
    private String exportFormat;
    
    /**
     * 包含纹理: 1-是, 0-否
     */
    private Integer includeTextures;
    
    /**
     * 包含材质: 1-是, 0-否
     */
    private Integer includeMaterials;
    
    // ========== 任务状态 ==========
    
    /**
     * 状态: pending/processing/completed/failed
     */
    private String status;
    
    /**
     * 进度百分比
     */
    private Integer progress;
    
    // ========== 文件信息 ==========
    
    /**
     * 输出文件路径
     */
    private String outputPath;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 下载链接
     */
    private String downloadUrl;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
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
    
    /**
     * 获取过期时间
     */
    public LocalDateTime getExpirationTime() {
        return expiresAt;
    }
    
    /**
     * 设置过期时间
     */
    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expiresAt = expirationTime;
    }
}