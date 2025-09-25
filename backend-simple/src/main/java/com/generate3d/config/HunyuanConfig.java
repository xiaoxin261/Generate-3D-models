package com.generate3d.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.hunyuan.v20230901.HunyuanClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 混元AI配置类
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.ai.hunyuan.secret-id")
public class HunyuanConfig {

    private final HunyuanProperties hunyuanProperties;

    public HunyuanConfig(HunyuanProperties hunyuanProperties) {
        this.hunyuanProperties = hunyuanProperties;
    }

    /**
     * 创建混元客户端
     */
    @Bean
    public HunyuanClient hunyuanClient() {
        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey
            Credential cred = new Credential(hunyuanProperties.getSecretId(), hunyuanProperties.getSecretKey());
            
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(hunyuanProperties.getEndpoint());
            httpProfile.setConnTimeout(hunyuanProperties.getTimeout());
            httpProfile.setReadTimeout(hunyuanProperties.getTimeout());
            
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            
            // 实例化要请求产品的client对象
            HunyuanClient client = new HunyuanClient(cred, hunyuanProperties.getRegion(), clientProfile);
            
            log.info("混元AI客户端初始化成功 - region: {}, endpoint: {}", 
                    hunyuanProperties.getRegion(), hunyuanProperties.getEndpoint());
            
            return client;
            
        } catch (Exception e) {
            log.error("混元AI客户端初始化失败", e);
            throw new RuntimeException("混元AI客户端初始化失败: " + e.getMessage());
        }
    }
}