package com.generate3d.validator;

import com.generate3d.dto.GenerateModelRequest;
import com.generate3d.exception.ValidationException;
import org.springframework.stereotype.Component;

/**
 * 基础参数验证器
 */
@Component
public class BasicParameterValidator {
    
    /**
     * 验证请求参数（Controller使用）
     */
    public void validateRequest(GenerateModelRequest request) {
        validateTextInput(request.getText());
        validateBasicParameters(request);
    }
    
    /**
     * 验证基础参数
     */
    public void validateBasicParameters(GenerateModelRequest request) {
        validateDimensions(request);
        validateDimensionRatio(request);
        validateStyle(request);
    }
    
    /**
     * 验证尺寸参数
     */
    private void validateDimensions(GenerateModelRequest request) {
        // 检查尺寸是否为正数
        if (request.getLength() == null || request.getLength() <= 0) {
            throw new ValidationException("长度必须大于0");
        }
        
        if (request.getWidth() == null || request.getWidth() <= 0) {
            throw new ValidationException("宽度必须大于0");
        }
        
        if (request.getHeight() == null || request.getHeight() <= 0) {
            throw new ValidationException("高度必须大于0");
        }
        
        // 检查尺寸上限
        if (request.getLength() > 50.0) {
            throw new ValidationException("长度不能超过50米");
        }
        
        if (request.getWidth() > 50.0) {
            throw new ValidationException("宽度不能超过50米");
        }
        
        if (request.getHeight() > 50.0) {
            throw new ValidationException("高度不能超过50米");
        }
        
        // 检查尺寸下限
        if (request.getLength() < 0.1) {
            throw new ValidationException("长度不能小于0.1米");
        }
        
        if (request.getWidth() < 0.1) {
            throw new ValidationException("宽度不能小于0.1米");
        }
        
        if (request.getHeight() < 0.1) {
            throw new ValidationException("高度不能小于0.1米");
        }
    }
    
    /**
     * 验证尺寸比例合理性
     */
    private void validateDimensionRatio(GenerateModelRequest request) {
        double maxDimension = Math.max(Math.max(request.getLength(), request.getWidth()), request.getHeight());
        double minDimension = Math.min(Math.min(request.getLength(), request.getWidth()), request.getHeight());
        
        // 检查比例是否过于极端
        if (maxDimension / minDimension > 100) {
            throw new ValidationException("模型尺寸比例过于极端，最大尺寸与最小尺寸的比例不能超过100:1");
        }
        
        // 检查总体积是否合理（防止生成过大的模型）
        double volume = request.getLength() * request.getWidth() * request.getHeight();
        if (volume > 10000) { // 10000立方米
            throw new ValidationException("模型体积过大，请适当减小尺寸参数");
        }
    }
    
    /**
     * 验证风格参数
     */
    private void validateStyle(GenerateModelRequest request) {
        if (request.getStyle() == null || request.getStyle().trim().isEmpty()) {
            throw new ValidationException("风格类型不能为空");
        }
        
        String[] allowedStyles = {"realistic", "cartoon", "minimalist", "industrial", "organic"};
        boolean isValidStyle = false;
        
        for (String style : allowedStyles) {
            if (style.equals(request.getStyle())) {
                isValidStyle = true;
                break;
            }
        }
        
        if (!isValidStyle) {
            throw new ValidationException("不支持的风格类型：" + request.getStyle() + 
                    "，支持的风格：realistic、cartoon、minimalist、industrial、organic");
        }
    }
    
    /**
     * 验证文本输入
     */
    public void validateTextInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("输入文本不能为空");
        }
        
        if (text.length() < 10) {
            throw new ValidationException("输入文本长度不能少于10个字符");
        }
        
        if (text.length() > 500) {
            throw new ValidationException("输入文本长度不能超过500个字符");
        }
    }
    
    /**
     * 获取推荐的尺寸范围提示
     */
    public String getDimensionRecommendation(String style) {
        if (style == null) {
            return "建议尺寸范围：长度0.1-50米，宽度0.1-50米，高度0.1-50米，长宽高比例不超过100:1";
        }
        
        switch (style.toLowerCase()) {
            case "realistic":
                return "现实风格建议：长度1-10米，宽度1-10米，高度1-10米，适合家具和建筑物";
            case "cartoon":
                return "卡通风格建议：长度0.5-5米，宽度0.5-5米，高度0.5-5米，可以使用夸张比例";
            case "minimalist":
                return "简约风格建议：长度0.5-20米，宽度0.5-20米，高度0.5-20米，注重简洁线条";
            case "industrial":
                return "工业风格建议：长度2-50米，宽度2-50米，高度2-50米，适合大型机械设备";
            case "organic":
                return "有机风格建议：长度0.1-10米，宽度0.1-10米，高度0.1-10米，可以使用不规则形状";
            default:
                return "建议尺寸范围：长度0.1-50米，宽度0.1-50米，高度0.1-50米，长宽高比例不超过100:1";
        }
    }
}