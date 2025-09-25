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
import java.util.Map;

/**
 * 混元AI服务类
 */
@Slf4j
@Service
@ConditionalOnBean(HunyuanClient.class)
public class HunyuanService {

    private final HunyuanClient hunyuanClient;
    private final HunyuanProperties hunyuanProperties;

    public HunyuanService(HunyuanClient hunyuanClient, HunyuanProperties hunyuanProperties) {
        this.hunyuanClient = hunyuanClient;
        this.hunyuanProperties = hunyuanProperties;
    }

    /**
     * 发送聊天请求
     *
     * @param prompt 用户输入的提示词
     * @return AI回复内容
     */
    public String chatCompletion(String prompt) {
        return chatCompletion(prompt, "hunyuan-lite");
    }

    /**
     * 发送聊天请求
     *
     * @param prompt 用户输入的提示词
     * @param model 模型名称
     * @return AI回复内容
     */
    public String chatCompletion(String prompt, String model) {
        try {
            // 实例化一个请求对象
            ChatCompletionsRequest req = new ChatCompletionsRequest();
            
            // 设置模型
            req.setModel(model);
            
            // 设置消息
            Message[] messages = new Message[1];
            Message message = new Message();
            message.setRole("user");
            message.setContent(prompt);
            messages[0] = message;
            req.setMessages(messages);
            
            // 设置其他参数
            req.setStream(false); // 不使用流式输出
            req.setTemperature(0.7f); // 温度参数
            req.setTopP(0.9f); // TopP参数
            
            log.info("发送混元AI请求 - model: {}, prompt: {}", model, prompt);
            
            // 返回的resp是一个ChatCompletionsResponse的实例，与请求对象对应
            ChatCompletionsResponse resp = hunyuanClient.ChatCompletions(req);
            
            if (resp.getChoices() != null && resp.getChoices().length > 0) {
                String content = resp.getChoices()[0].getMessage().getContent();
                log.info("混元AI响应成功 - content length: {}", content.length());
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

    /**
     * 生成3D模型描述
     *
     * @param userPrompt 用户输入的描述
     * @return 优化后的3D模型描述
     */
    public String generate3DModelPrompt(String userPrompt) {
        String systemPrompt = "你是一个专业的3D建模助手。请根据用户的描述，生成一个详细、准确的3D模型描述，" +
                "包括物体的形状、材质、颜色、尺寸比例等关键信息。描述要具体且适合3D建模软件理解。\n\n" +
                "用户描述：" + userPrompt;
        
        return chatCompletion(systemPrompt);
    }

    /**
     * 从图片生成3D模型
     * 注意：这是一个模拟实现，实际的混元AI图片生成3D模型API可能有所不同
     *
     * @param imageUrl 图片URL
     * @param params 生成参数
     * @return 3D模型数据
     */
    public String generateModelFromImage(String imageUrl, Map<String, Object> params) {
        try {
            log.info("开始调用混元AI图片生成3D模型 - imageUrl: {}, params: {}", imageUrl, params);
            
            // 模拟调用混元AI的图片生成3D模型接口
            // 实际实现中，这里应该调用真正的混元AI图片生成3D模型API
            
            // 构建请求参数
            String prompt = buildImageTo3DPrompt(imageUrl, params);
            
            // 调用AI服务（这里使用文本生成作为模拟）
            String aiResponse = chatCompletion(prompt, "hunyuan-lite");
            
            // 模拟生成3D模型数据
            String modelData = generateMockModelData(params, aiResponse);
            
            log.info("混元AI图片生成3D模型完成 - 数据长度: {}", modelData.length());
            return modelData;
            
        } catch (Exception e) {
            log.error("混元AI图片生成3D模型失败", e);
            throw new RuntimeException("图片生成3D模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建图片生成3D模型的提示词
     */
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
    
    /**
     * 生成模拟的3D模型数据
     */
    private String generateMockModelData(Map<String, Object> params, String aiResponse) {
        // 这里生成一个简单的OBJ格式3D模型数据作为示例
        StringBuilder objData = new StringBuilder();
        objData.append("# 基于图片生成的3D模型\n");
        objData.append("# AI描述: ").append(aiResponse.substring(0, Math.min(100, aiResponse.length()))).append("\n");
        objData.append("\n");
        
        // 获取尺寸参数
        double length = getDoubleParam(params, "length", 6.0);
        double width = getDoubleParam(params, "width", 4.0);
        double height = getDoubleParam(params, "height", 3.0);
        
        // 生成一个简单的立方体
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
        objData.append("f 1 2 3 4\n"); // 底面
        objData.append("f 5 8 7 6\n"); // 顶面
        objData.append("f 1 5 6 2\n"); // 前面
        objData.append("f 2 6 7 3\n"); // 右面
        objData.append("f 3 7 8 4\n"); // 后面
        objData.append("f 4 8 5 1\n"); // 左面
        
        return objData.toString();
    }
    
    /**
     * 获取Double类型参数
     */
    private double getDoubleParam(Map<String, Object> params, String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /**
     * 检查服务是否可用
     *
     * @return 是否可用
     */
    public boolean isServiceAvailable() {
        try {
            String testResponse = chatCompletion("测试连接", "hunyuan-lite");
            return testResponse != null && !testResponse.trim().isEmpty();
        } catch (Exception e) {
            log.error("混元AI服务不可用", e);
            return false;
        }
    }
}