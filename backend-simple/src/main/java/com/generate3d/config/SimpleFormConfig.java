package com.generate3d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 简单表单配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.form")
public class SimpleFormConfig {
    
    /**
     * 长度配置
     */
    private DimensionConfig length = new DimensionConfig(0.1, 50.0, 1.0, 0.1, "米");
    
    /**
     * 宽度配置
     */
    private DimensionConfig width = new DimensionConfig(0.1, 50.0, 1.0, 0.1, "米");
    
    /**
     * 高度配置
     */
    private DimensionConfig height = new DimensionConfig(0.1, 50.0, 1.0, 0.1, "米");
    
    /**
     * 风格选项
     */
    private List<String> styleOptions = Arrays.asList(
            "realistic", "cartoon", "minimalist", "industrial", "organic"
    );
    
    /**
     * 默认风格
     */
    private String defaultStyle = "realistic";
    
    /**
     * 文本输入配置
     */
    private TextInputConfig textInput = new TextInputConfig();
    
    /**
     * 文本输入配置类
     */
    @Data
    public static class TextInputConfig {
        private int minLength = 5;
        private int maxLength = 200;
        private String placeholder = "请描述您想要生成的3D模型...";
        private List<String> suggestions = Arrays.asList(
                "一个现代风格的椅子",
                "简约的桌子",
                "工业风格的灯具",
                "有机形状的花瓶"
        );
    }
}