package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 尺寸配置类
 */
@Data
@Builder
@Schema(description = "尺寸配置")
public class DimensionConfig {
    
    @Schema(description = "最小值", example = "0.1")
    private Double min;
    
    @Schema(description = "最大值", example = "50.0")
    private Double max;
    
    @Schema(description = "默认值", example = "6.0")
    private Double defaultValue;
    
    @Schema(description = "步长", example = "0.1")
    private Double step;
    
    @Schema(description = "单位", example = "m")
    private String unit;
}