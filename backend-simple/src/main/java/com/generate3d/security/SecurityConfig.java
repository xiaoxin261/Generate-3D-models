package com.generate3d.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 */
@Configuration
// @EnableWebSecurity  // 禁用Spring Security
// @EnableMethodSecurity  // 禁用方法级安全
@RequiredArgsConstructor
public class SecurityConfig {
    
    // private final JwtAuthenticationFilter jwtAuthenticationFilter;  // 禁用JWT过滤器
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // @Bean  // 禁用Security配置
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //     http
    //         .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    //         .csrf(AbstractHttpConfigurer::disable)
    //         .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    //         .authorizeHttpRequests(authz -> authz
    //             // 公开接口 - 按照从具体到一般的顺序配置
    //             .requestMatchers("/auth/**").permitAll()
    //             .requestMatchers("/public/**").permitAll()
    //             .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    //             .requestMatchers("/actuator/health").permitAll()
    //             
    //             // 用户相关接口 - 具体路径优先
    //             .requestMatchers("/user/info").permitAll() // 允许公开访问用户信息接口
    //             .requestMatchers("/user/**").authenticated() // 其他用户接口需要认证
    //             
    //             // 3D模型生成相关接口 - 暂时允许公开访问
    //             .requestMatchers("/api/v1/models/**").permitAll()
    //             .requestMatchers("/api/v1/jobs/**").permitAll()
    //             
    //             // 文件管理接口
    //             .requestMatchers("/file/**").permitAll()
    //             
    //             // 管理员接口
    //             .requestMatchers("/admin/**").hasRole("ADMIN")
    //             
    //             // 其他接口暂时允许访问（兼容现有功能）
    //             .anyRequest().permitAll()
    //         )
    //         .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    //     
    //     return http.build();
    // }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}