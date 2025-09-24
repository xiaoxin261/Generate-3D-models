package com.generate3d.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS配置类
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "oss")
public class OssConfig {

    private final AppStorageProperties storageProperties;

    public OssConfig(AppStorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Bean
    public OSS ossClient() {
        AppStorageProperties.Oss ossConfig = storageProperties.getOss();
        
        log.info("初始化OSS客户端 - endpoint: {}, bucket: {}", 
                ossConfig.getEndpoint(), ossConfig.getBucket());
        
        return new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
    }
}