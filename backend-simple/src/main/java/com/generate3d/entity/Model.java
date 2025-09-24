package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 3D模型实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_model")
public class Model {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模型唯一标识
     */
    private String modelId;
    
    /**
     * 模型名称
     */
    private String name;
    
    /**
     * 模型描述
     */
    private String description;
    
    /**
     * 原始输入文本
     */
    private String originalText;
    
    /**
     * 模型分类
     */
    private String category;
    
    // ========== 文件信息 ==========
    
    /**
     * 模型文件路径
     */
    private String filePath;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件格式
     */
    private String fileFormat;
    
    /**
     * 缩略图路径
     */
    private String thumbnailPath;
    
    // ========== 模型属性 ==========
    
    /**
     * 顶点数
     */
    private Integer verticesCount;
    
    /**
     * 面数
     */
    private Integer facesCount;
    
    /**
     * 包围盒最小值
     */
    private String boundingBoxMin;
    
    /**
     * 包围盒最大值
     */
    private String boundingBoxMax;
    
    // ========== 生成参数 ==========
    
    /**
     * 生成参数(JSON格式)
     */
    private String generationParams;
    
    /**
     * 材质类型
     */
    private String materialType;
    
    /**
     * 主色调
     */
    private String primaryColor;
    
    /**
     * 模型尺寸
     */
    private String modelSize;
    
    // ========== 状态信息 ==========
    
    /**
     * 状态: 1-正常, 0-删除
     */
    private Integer status;
    
    /**
     * 是否收藏: 1-是, 0-否
     */
    private Integer favorite;
    
    /**
     * 生成耗时(秒)
     */
    private Integer generationTime;
    
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
     * 获取边界框信息
     */
    public String getBoundingBox() {
        if (boundingBoxMin != null && boundingBoxMax != null) {
            return String.format("{\"min\":%s,\"max\":%s}", boundingBoxMin, boundingBoxMax);
        }
        return null;
    }
}