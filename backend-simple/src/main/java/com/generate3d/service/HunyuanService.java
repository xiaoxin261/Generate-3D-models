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