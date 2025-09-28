package com.generate3d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * CORS跨域测试控制器
 * 用于验证跨域配置是否正常工作
 */
@Slf4j
@RestController
@RequestMapping("/api/cors-test")
@Tag(name = "CORS测试", description = "跨域配置测试接口")
public class CorsTestController {

    /**
     * GET请求测试
     */
    @GetMapping("/get")
    @Operation(summary = "GET请求测试", description = "测试简单GET请求的跨域支持")
    public ResponseEntity<Map<String, Object>> testGet(HttpServletRequest request) {
        log.info("收到CORS GET测试请求 - Origin: {}", request.getHeader("Origin"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET");
        response.put("message", "CORS GET请求测试成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("origin", request.getHeader("Origin"));
        response.put("userAgent", request.getHeader("User-Agent"));
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST请求测试
     */
    @PostMapping("/post")
    @Operation(summary = "POST请求测试", description = "测试复杂POST请求的跨域支持")
    public ResponseEntity<Map<String, Object>> testPost(
            @RequestBody(required = false) Map<String, Object> requestBody,
            HttpServletRequest request) {
        
        log.info("收到CORS POST测试请求 - Origin: {}, Body: {}", 
                request.getHeader("Origin"), requestBody);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST");
        response.put("message", "CORS POST请求测试成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("origin", request.getHeader("Origin"));
        response.put("requestBody", requestBody);
        response.put("contentType", request.getContentType());
        
        return ResponseEntity.ok(response);
    }

    /**
     * PUT请求测试
     */
    @PutMapping("/put")
    @Operation(summary = "PUT请求测试", description = "测试PUT请求的跨域支持")
    public ResponseEntity<Map<String, Object>> testPut(
            @RequestBody(required = false) Map<String, Object> requestBody,
            HttpServletRequest request) {
        
        log.info("收到CORS PUT测试请求 - Origin: {}", request.getHeader("Origin"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "PUT");
        response.put("message", "CORS PUT请求测试成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("origin", request.getHeader("Origin"));
        response.put("requestBody", requestBody);
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE请求测试
     */
    @DeleteMapping("/delete")
    @Operation(summary = "DELETE请求测试", description = "测试DELETE请求的跨域支持")
    public ResponseEntity<Map<String, Object>> testDelete(HttpServletRequest request) {
        log.info("收到CORS DELETE测试请求 - Origin: {}", request.getHeader("Origin"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "DELETE");
        response.put("message", "CORS DELETE请求测试成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("origin", request.getHeader("Origin"));
        
        return ResponseEntity.ok(response);
    }

    /**
     * OPTIONS预检请求测试
     */
    @RequestMapping(value = "/options", method = RequestMethod.OPTIONS)
    @Operation(summary = "OPTIONS预检请求测试", description = "测试OPTIONS预检请求的处理")
    public ResponseEntity<Void> testOptions(HttpServletRequest request) {
        log.info("收到CORS OPTIONS预检请求 - Origin: {}, Method: {}, Headers: {}", 
                request.getHeader("Origin"),
                request.getHeader("Access-Control-Request-Method"),
                request.getHeader("Access-Control-Request-Headers"));
        
        return ResponseEntity.ok().build();
    }

    /**
     * 带自定义头的请求测试
     */
    @PostMapping("/custom-headers")
    @Operation(summary = "自定义请求头测试", description = "测试带有自定义请求头的跨域支持")
    public ResponseEntity<Map<String, Object>> testCustomHeaders(
            @RequestHeader(value = "X-Custom-Header", required = false) String customHeader,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        
        log.info("收到带自定义头的CORS测试请求 - Origin: {}, Custom-Header: {}, Authorization: {}", 
                request.getHeader("Origin"), customHeader, authorization);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST");
        response.put("message", "CORS自定义请求头测试成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("origin", request.getHeader("Origin"));
        response.put("customHeader", customHeader);
        response.put("hasAuthorization", authorization != null);
        
        // 设置自定义响应头
        return ResponseEntity.ok()
                .header("X-Response-Custom", "test-value")
                .header("X-Request-ID", "cors-test-" + System.currentTimeMillis())
                .body(response);
    }

    /**
     * 获取CORS配置信息
     */
    @GetMapping("/config")
    @Operation(summary = "获取CORS配置信息", description = "返回当前的CORS配置状态")
    public ResponseEntity<Map<String, Object>> getCorsConfig(HttpServletRequest request) {
        log.info("获取CORS配置信息请求 - Origin: {}", request.getHeader("Origin"));
        
        Map<String, Object> config = new HashMap<>();
        config.put("corsEnabled", true);
        config.put("allowedOrigins", "所有域名（开发环境）");
        config.put("allowedMethods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        config.put("allowedHeaders", "所有请求头");
        config.put("allowCredentials", true);
        config.put("maxAge", 3600);
        config.put("currentOrigin", request.getHeader("Origin"));
        config.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(config);
    }
}