package com.generate3d.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

/**
 * 3D模型生成请求DTO
 */
@Data
@Schema(description = "3D模型生成请求")
public class GenerateModelRequest {
    
    @NotBlank(message = "输入文本不能为空")
    @Size(min = 10, max = 500, message = "输入文本长度必须在10-500字符之间")
    @Schema(description = "输入文本描述", example = "一只可爱的小猫咪，坐在草地上")
    private String text;
    
    @DecimalMin(value = "0.1", message = "长度必须大于0.1米")
    @DecimalMax(value = "50.0", message = "长度不能超过50米")
    @Schema(description = "长度(m)", example = "6.0", minimum = "0.1", maximum = "50.0")
    private Double length = 6.0;
    
    @DecimalMin(value = "0.1", message = "宽度必须大于0.1米")
    @DecimalMax(value = "50.0", message = "宽度不能超过50米")
    @Schema(description = "宽度(m)", example = "4.0", minimum = "0.1", maximum = "50.0")
    private Double width = 4.0;
    
    @DecimalMin(value = "0.1", message = "高度必须大于0.1米")
    @DecimalMax(value = "50.0", message = "高度不能超过50米")
    @Schema(description = "高度(m)", example = "3.0", minimum = "0.1", maximum = "50.0")
    private Double height = 3.0;
    
    @Schema(description = "风格类型", example = "现代", 
            allowableValues = {"现代", "古典", "简约", "复古", "工业", "自然"})
    private String style = "现代";
}