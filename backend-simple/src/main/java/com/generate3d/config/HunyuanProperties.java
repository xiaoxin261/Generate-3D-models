package com.generate3d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 混元AI配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai.hunyuan")
public class HunyuanProperties {
    
    /**
     * 腾讯云SecretId
     */
    private String secretId;
    
    /**
     * 腾讯云SecretKey
     */
    private String secretKey;
    
    /**
     * 地域
     */
    private String region = "ap-beijing";
    
    /**
     * 接口地址
     */
    private String endpoint = "hunyuan.tencentcloudapi.com";
    
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;
    
    /**
     * 重试次数
     */
    private Integer retryTimes = 3;
    
    /**
     * 最大并发请求数
     */
    private Integer maxConcurrentRequests = 10;
}