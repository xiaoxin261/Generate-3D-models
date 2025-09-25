package com.generate3d.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 尺寸配置类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionConfig {
    
    /**
     * 最小值
     */
    private Double min;
    
    /**
     * 最大值
     */
    private Double max;
    
    /**
     * 默认值
     */
    private Double defaultValue;
    
    /**
     * 步长
     */
    private Double step;
    
    /**
     * 单位
     */
    private String unit;
}