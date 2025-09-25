package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 简单表单配置类
 */
@Data
@Builder
@Schema(description = "简单表单配置")
public class SimpleFormConfig {
    
    @Schema(description = "长度配置")
    private DimensionConfig lengthConfig;
    
    @Schema(description = "宽度配置")
    private DimensionConfig widthConfig;
    
    @Schema(description = "高度配置")
    private DimensionConfig heightConfig;
    
    @Schema(description = "风格选项列表")
    private List<String> styleOptions;
    
    @Schema(description = "默认风格", example = "现代")
    private String defaultStyle;
    
    @Schema(description = "文本输入配置")
    private TextInputConfig textConfig;
    
    /**
     * 文本输入配置
     */
    @Data
    @Builder
    @Schema(description = "文本输入配置")
    public static class TextInputConfig {
        
        @Schema(description = "最小长度", example = "10")
        private Integer minLength;
        
        @Schema(description = "最大长度", example = "500")
        private Integer maxLength;
        
        @Schema(description = "占位符文本", example = "请输入3D模型描述...")
        private String placeholder;
        
        @Schema(description = "输入提示")
        private List<String> suggestions;
    }
}