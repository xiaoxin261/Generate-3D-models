package com.generate3d.service;

import com.generate3d.config.HunyuanProperties;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.hunyuan.v20230901.HunyuanClient;
import com.tencentcloudapi.hunyuan.v20230901.models.ChatCompletionsRequest;
import com.tencentcloudapi.hunyuan.v20230901.models.ChatCompletionsResponse;
import com.tencentcloudapi.hunyuan.v20230901.models.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 优化版混元AI服务类
 * 集成缓存、频率限制、批量处理等优化功能
 */
@Slf4j
@Service
@ConditionalOnBean(HunyuanClient.class)
public class OptimizedHunyuanService {

    private final HunyuanClient hunyuanClient;
    private final HunyuanProperties hunyuanProperties;
    private final AICallOptimizationService optimizationService;
    
    // 异步执行器
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(5);

    public OptimizedHunyuanService(HunyuanClient hunyuanClient, 
                                 HunyuanProperties hunyuanProperties,
                                 AICallOptimizationService optimizationService) {
        this.hunyuanClient = hunyuanClient;
        this.hunyuanProperties = hunyuanProperties;
        this.optimizationService = optimizationService;
    }

    /**
     * 优化版聊天完成接口
     */
    public String chatCompletion(String prompt, String userId) {
        return chatCompletion(prompt, "hunyuan-lite", userId, new HashMap<>());
    }
    
    /**
     * 优化版聊天完成接口（带参数）
     */
    public String chatCompletion(String prompt, String model, String userId, Map<String, Object> params) {
        // 1. 检查频率限制
        if (!optimizationService.canCallAI(userId)) {
            int remaining = optimizationService.getRemainingCalls(userId);
            throw new RuntimeException("调用频率超限，剩余次数: " + remaining);
        }
        
        // 2. 尝试从缓存获取
        String cachedResponse = optimizationService.getCachedResponse(prompt, model, params);
        if (cachedResponse != null) {
            log.info("使用缓存响应 - userId: {}, prompt length: {}", userId, prompt.length());
            return cachedResponse;
        }
        
        // 3. 调用AI服务
        String response = callHunyuanAPI(prompt, model, params);
        
        // 4. 缓存响应
        if (StringUtils.hasText(response)) {
            optimizationService.cacheResponse(prompt, model, params, response);
        }
        
        return response;
    }
    
    /**
     * 异步聊天完成
     */
    public CompletableFuture<String> chatCompletionAsync(String prompt, String model, String userId, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chatCompletion(prompt, model, userId, params);
            } catch (Exception e) {
                log.error("异步AI调用失败", e);
                throw new RuntimeException("异步AI调用失败: " + e.getMessage());
            }
        }, asyncExecutor);
    }
    
    /**
     * 优化版3D模型提示词生成
     */
    public String generate3DModelPrompt(String userPrompt, String userId) {
        String systemPrompt = "你是一个专业的3D建模助手。请根据用户的描述，生成一个详细、准确的3D模型描述，" +
                "包括物体的形状、材质、颜色、尺寸比例等关键信息。描述要具体且适合3D建模软件理解。\n\n" +
                "用户描述：" + userPrompt;
        
        Map<String, Object> params = new HashMap<>();
        params.put("type", "3d_model_prompt");
        params.put("temperature", 0.7);
        
        return chatCompletion(systemPrompt, "hunyuan-lite", userId, params);
    }
    
    /**
     * 优化版图片生成3D模型
     */
    public String generateModelFromImage(String imageUrl, Map<String, Object> params, String userId) {
        try {
            log.info("开始优化版图片生成3D模型 - userId: {}, imageUrl: {}", userId, imageUrl);
            
            // 1. 检查频率限制
            if (!optimizationService.canCallAI(userId)) {
                throw new RuntimeException("调用频率超限");
            }
            
            // 2. 构建请求参数
            String prompt = buildImageTo3DPrompt(imageUrl, params);
            Map<String, Object> aiParams = new HashMap<>(params);
            aiParams.put("type", "image_to_3d");
            
            // 3. 尝试从缓存获取
            String cachedResponse = optimizationService.getCachedResponse(prompt, "hunyuan-lite", aiParams);
            if (cachedResponse != null) {
                log.info("使用缓存的3D模型数据 - userId: {}", userId);
                return cachedResponse;
            }
            
            // 4. 调用AI服务
            String aiResponse = callHunyuanAPI(prompt, "hunyuan-lite", aiParams);
            
            // 5. 生成3D模型数据
            String modelData = generateMockModelData(params, aiResponse);
            
            // 6. 缓存结果
            optimizationService.cacheResponse(prompt, "hunyuan-lite", aiParams, modelData);
            
            log.info("优化版图片生成3D模型完成 - userId: {}, 数据长度: {}", userId, modelData.length());
            return modelData;
            
        } catch (Exception e) {
            log.error("优化版图片生成3D模型失败 - userId: {}", userId, e);
            throw new RuntimeException("图片生成3D模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量生成3D模型提示词
     */
    public Map<String, String> batchGenerate3DModelPrompts(Map<String, String> userPrompts, String userId) {
        // 1. 检查频率限制
        if (!optimizationService.canCallAI(userId)) {
            throw new RuntimeException("调用频率超限");
        }
        
        // 2. 构建批量请求
        var batchRequests = userPrompts.entrySet().stream()
            .map(entry -> {
                String systemPrompt = "你是一个专业的3D建模助手。请根据用户的描述，生成一个详细、准确的3D模型描述，" +
                        "包括物体的形状、材质、颜色、尺寸比例等关键信息。描述要具体且适合3D建模软件理解。\n\n" +
                        "用户描述：" + entry.getValue();
                
                Map<String, Object> params = new HashMap<>();
                params.put("type", "3d_model_prompt");
                params.put("temperature", 0.7);
                
                return new AICallOptimizationService.BatchRequest(
                    entry.getKey(), systemPrompt, "hunyuan-lite", params
                );
            })
            .toList();
        
        // 3. 批量处理
        return optimizationService.batchProcessSimilarRequests(batchRequests);
    }
    
    /**
     * 获取服务状态和统计信息
     */
    public Map<String, Object> getServiceStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户调用统计
        stats.put("remaining_calls", optimizationService.getRemainingCalls(userId));
        stats.put("service_available", isServiceAvailable());
        
        // 缓存统计
        stats.putAll(optimizationService.getCacheStats());
        
        // 服务配置
        stats.put("rate_limit_per_hour", 100);
        stats.put("cache_ttl_hours", 24);
        
        return stats;
    }
    
    /**
     * 预热缓存
     */
    public void warmupCache(String userId) {
        log.info("开始预热缓存 - userId: {}", userId);
        
        // 预热常用的3D模型提示词
        String[] commonPrompts = {
            "生成一个简单的立方体",
            "创建一个圆形桌子",
            "设计一把现代椅子",
            "制作一个花瓶",
            "构建一个房屋模型"
        };
        
        for (String prompt : commonPrompts) {
            try {
                generate3DModelPrompt(prompt, userId);
                Thread.sleep(100); // 避免频率限制
            } catch (Exception e) {
                log.warn("预热缓存失败: {}", prompt, e);
            }
        }
        
        log.info("缓存预热完成 - userId: {}", userId);
    }
    
    // 私有方法
    
    private String callHunyuanAPI(String prompt, String model, Map<String, Object> params) {
        try {
            ChatCompletionsRequest req = new ChatCompletionsRequest();
            req.setModel(model);
            
            Message[] messages = new Message[1];
            Message message = new Message();
            message.setRole("user");
            message.setContent(prompt);
            messages[0] = message;
            req.setMessages(messages);
            
            // 设置参数
            req.setStream(false);
            req.setTemperature(getFloatParam(params, "temperature", 0.7f));
            req.setTopP(getFloatParam(params, "top_p", 0.9f));
            
            log.debug("调用混元AI - model: {}, prompt length: {}", model, prompt.length());
            
            ChatCompletionsResponse resp = hunyuanClient.ChatCompletions(req);
            
            if (resp.getChoices() != null && resp.getChoices().length > 0) {
                String content = resp.getChoices()[0].getMessage().getContent();
                log.debug("混元AI响应成功 - content length: {}", content.length());
                return content;
            } else {
                log.warn("混元AI响应为空");
                return "";
            }
            
        } catch (TencentCloudSDKException e) {
            log.error("混元AI请求失败 - code: {}, message: {}", e.getErrorCode(), e.getMessage());
            throw new RuntimeException("混元AI请求失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("混元AI请求异常", e);
            throw new RuntimeException("混元AI请求异常: " + e.getMessage());
        }
    }
    
    private String buildImageTo3DPrompt(String imageUrl, Map<String, Object> params) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("基于提供的图片生成3D模型，请分析图片内容并生成相应的3D模型描述。");
        prompt.append("\n图片URL: ").append(imageUrl);
        
        if (params.containsKey("style")) {
            prompt.append("\n风格: ").append(params.get("style"));
        }
        if (params.containsKey("quality")) {
            prompt.append("\n质量: ").append(params.get("quality"));
        }
        if (params.containsKey("preserveDetails")) {
            prompt.append("\n保持细节: ").append(params.get("preserveDetails"));
        }
        
        prompt.append("\n尺寸: ");
        prompt.append(params.getOrDefault("length", 6.0)).append("m × ");
        prompt.append(params.getOrDefault("width", 4.0)).append("m × ");
        prompt.append(params.getOrDefault("height", 3.0)).append("m");
        
        prompt.append("\n\n请生成详细的3D模型描述，包括形状、材质、颜色等信息。");
        
        return prompt.toString();
    }
    
    private String generateMockModelData(Map<String, Object> params, String aiResponse) {
        StringBuilder objData = new StringBuilder();
        objData.append("# 优化版基于图片生成的3D模型\n");
        objData.append("# AI描述: ").append(aiResponse.substring(0, Math.min(100, aiResponse.length()))).append("\n");
        objData.append("\n");
        
        double length = getDoubleParam(params, "length", 6.0);
        double width = getDoubleParam(params, "width", 4.0);
        double height = getDoubleParam(params, "height", 3.0);
        
        // 生成优化的立方体模型
        objData.append("# 顶点\n");
        objData.append(String.format("v 0.0 0.0 0.0\n"));
        objData.append(String.format("v %.1f 0.0 0.0\n", length));
        objData.append(String.format("v %.1f %.1f 0.0\n", length, width));
        objData.append(String.format("v 0.0 %.1f 0.0\n", width));
        objData.append(String.format("v 0.0 0.0 %.1f\n", height));
        objData.append(String.format("v %.1f 0.0 %.1f\n", length, height));
        objData.append(String.format("v %.1f %.1f %.1f\n", length, width, height));
        objData.append(String.format("v 0.0 %.1f %.1f\n", width, height));
        
        objData.append("\n# 面\n");
        objData.append("f 1 2 3 4\n");
        objData.append("f 5 8 7 6\n");
        objData.append("f 1 5 6 2\n");
        objData.append("f 2 6 7 3\n");
        objData.append("f 3 7 8 4\n");
        objData.append("f 4 8 5 1\n");
        
        return objData.toString();
    }
    
    private float getFloatParam(Map<String, Object> params, String key, float defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue;
    }
    
    private double getDoubleParam(Map<String, Object> params, String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    public boolean isServiceAvailable() {
        try {
            String testResponse = callHunyuanAPI("测试连接", "hunyuan-lite", new HashMap<>());
            return testResponse != null && !testResponse.trim().isEmpty();
        } catch (Exception e) {
            log.error("混元AI服务不可用", e);
            return false;
        }
    }
}