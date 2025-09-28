package com.generate3d.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS跨域配置类
 * 统一管理所有跨域相关配置
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * CORS配置源
     * 这是Spring Security和Spring MVC都会使用的配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("初始化CORS配置源");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（支持通配符模式）
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*",
            "http://*.generate3d.com",
            "https://*.generate3d.com",
            "*" // 开发环境允许所有源，生产环境应该限制
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "*" // 允许所有请求头
        ));
        
        // 暴露的响应头（前端可以访问的响应头）
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Content-Length",
            "Content-Disposition",
            "X-Total-Count",
            "X-Request-ID",
            "X-Response-Time",
            "Cache-Control",
            "Pragma",
            "Expires"
        ));
        
        // 是否允许发送Cookie和认证信息
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        // 注册CORS配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS配置完成 - 允许的源: {}, 允许的方法: {}", 
                configuration.getAllowedOriginPatterns(), 
                configuration.getAllowedMethods());
        
        return source;
    }
    
    /**
     * CORS过滤器
     * 提供额外的CORS处理能力
     */
    @Bean
    public CorsFilter corsFilter() {
        log.info("初始化CORS过滤器");
        return new CorsFilter(corsConfigurationSource());
    }
    
    /**
     * 开发环境CORS配置
     * 更宽松的配置，适用于开发调试
     */
    @Bean
    public CorsConfigurationSource devCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 开发环境允许所有源
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(86400L); // 24小时缓存
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * 生产环境CORS配置
     * 更严格的安全配置
     */
    public CorsConfigurationSource prodCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 生产环境只允许特定域名
        configuration.setAllowedOrigins(Arrays.asList(
            "https://www.generate3d.com",
            "https://app.generate3d.com",
            "https://admin.generate3d.com"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L); // 30分钟缓存
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}