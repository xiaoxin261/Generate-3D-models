package com.generate3d.controller;

import com.generate3d.common.Result;
import com.generate3d.dto.GenerateModelRequest;
import com.generate3d.dto.ImageTo3DRequest;
import com.generate3d.dto.ModelResponse;
import com.generate3d.config.SimpleFormConfig;
import com.generate3d.validator.BasicParameterValidator;
import com.generate3d.validator.ImageParameterValidator;
import com.generate3d.service.ModelGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 3D模型生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/models")
@Tag(name = "3D模型生成", description = "3D模型生成相关接口")
@RequiredArgsConstructor
public class ModelController {
    
    private final ModelGenerationService generationService;
    private final BasicParameterValidator parameterValidator;
    private final ImageParameterValidator imageParameterValidator;
    private final SimpleFormConfig formConfig;
    
    /**
     * 生成3D模型
     */
    @PostMapping("/generate")
    @Operation(summary = "生成3D模型", description = "根据文本描述和参数生成3D模型",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = false)
    public Result<ModelResponse> generateModel(@RequestBody @Valid GenerateModelRequest request) {
        try {
            // 参数验证
            parameterValidator.validateRequest(request);
            
            // 调用生成服务
            ModelResponse response = generationService.generateModel(request);
            
            log.info("模型生成请求成功，文本: {}, 尺寸: {}x{}x{}, 风格: {}", 
                    request.getText(), request.getLength(), request.getWidth(), 
                    request.getHeight(), request.getStyle());
            
            return Result.success("模型生成成功", response);
            
        } catch (Exception e) {
            log.error("模型生成失败", e);
            return Result.error("模型生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 图片生成3D模型
     */
    @PostMapping("/generate-from-image")
    @Operation(summary = "图片生成3D模型", description = "根据上传的图片和参数生成3D模型",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = false)
    public Result<ModelResponse> generateModelFromImage(@ModelAttribute @Valid ImageTo3DRequest request) {
        try {
            // 参数验证
            imageParameterValidator.validateRequest(request);
            
            // 调用生成服务
            ModelResponse response = generationService.generateModelFromImage(request);
            
            log.info("图片生成3D模型请求成功，文件名: {}, 尺寸: {}x{}x{}, 风格: {}", 
                    request.getImage().getOriginalFilename(), request.getLength(), 
                    request.getWidth(), request.getHeight(), request.getStyle());
            
            return Result.success("模型生成成功", response);
            
        } catch (Exception e) {
            log.error("图片生成3D模型失败", e);
            return Result.error("模型生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取可用的风格选项
     */
    @GetMapping("/styles")
    @Operation(summary = "获取风格选项", description = "获取所有可用的模型风格")
    public Result<List<String>> getAvailableStyles() {
        List<String> styles = formConfig.getStyleOptions();
        return Result.success("获取成功", styles);
    }
    
    /**
     * 获取表单配置
     */
    @GetMapping("/form-config")
    @Operation(summary = "获取表单配置", description = "获取生成表单的配置信息")
    public Result<SimpleFormConfig> getFormConfig() {
        return Result.success("获取成功", formConfig);
    }
    
    /**
     * 验证参数
     */
    @PostMapping("/validate")
    @Operation(summary = "验证参数", description = "验证生成参数是否有效")
    public Result<String> validateParameters(@RequestBody @Valid GenerateModelRequest request) {
        try {
            parameterValidator.validateRequest(request);
            return Result.success("参数验证通过", "所有参数都有效");
        } catch (Exception e) {
            return Result.error("参数验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取尺寸建议
     */
    @GetMapping("/dimension-recommendation")
    @Operation(summary = "获取尺寸建议", description = "根据风格获取推荐的尺寸")
    public Result<String> getDimensionRecommendation(@RequestParam String style) {
        String recommendation = parameterValidator.getDimensionRecommendation(style);
        return Result.success("获取成功", recommendation);
    }
}