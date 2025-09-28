package com.generate3d.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger API文档配置
 */
@Slf4j
@Configuration
public class SwaggerConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    /**
     * OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // 服务器配置
        Server localServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("本地开发环境");
        
        Server devServer = new Server()
                .url("http://dev.generate3d.com" + contextPath)
                .description("开发环境");
        
        Server prodServer = new Server()
                .url("https://api.generate3d.com" + contextPath)
                .description("生产环境");
        
        // 联系人信息
        Contact contact = new Contact()
                .name("Generate3D Team")
                .email("support@generate3d.com")
                .url("https://www.generate3d.com");
        
        // 许可证信息
        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
        
        // API信息
        Info info = new Info()
                .title("Generate3D API")
                .description("3D模型生成平台API文档")
                .version("1.0.0")
                .contact(contact)
                .license(license)
                .summary("基于AI的3D模型生成服务")
                .termsOfService("https://www.generate3d.com/terms");
        
        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .servers(List.of(localServer, devServer, prodServer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证，格式：Bearer <token>")
                        )
                );
        
        log.info("Swagger API文档配置完成，当前环境: {}", activeProfile);
        
        return openAPI;
    }
}