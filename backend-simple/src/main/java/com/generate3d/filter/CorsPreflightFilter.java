package com.generate3d.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS预检请求过滤器
 * 专门处理OPTIONS预检请求，确保复杂跨域请求能够正常工作
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsPreflightFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("初始化CORS预检请求过滤器");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String origin = httpRequest.getHeader("Origin");
        String method = httpRequest.getMethod();
        
        // 记录请求信息
        if (log.isDebugEnabled()) {
            log.debug("处理请求 - Method: {}, Origin: {}, URI: {}", 
                    method, origin, httpRequest.getRequestURI());
        }
        
        // 设置CORS响应头
        setCorsHeaders(httpResponse, origin);
        
        // 如果是OPTIONS预检请求，直接返回成功响应
        if ("OPTIONS".equalsIgnoreCase(method)) {
            handlePreflightRequest(httpRequest, httpResponse);
            return;
        }
        
        // 继续处理其他请求
        chain.doFilter(request, response);
    }
    
    /**
     * 设置CORS响应头
     */
    private void setCorsHeaders(HttpServletResponse response, String origin) {
        // 允许的源
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        
        // 允许的方法
        response.setHeader("Access-Control-Allow-Methods", 
                "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        
        // 允许的请求头
        response.setHeader("Access-Control-Allow-Headers", 
                "Origin, X-Requested-With, Content-Type, Accept, Authorization, " +
                "X-Custom-Header, X-Request-ID, Cache-Control, Pragma");
        
        // 暴露的响应头
        response.setHeader("Access-Control-Expose-Headers", 
                "Authorization, Content-Type, Content-Length, Content-Disposition, " +
                "X-Total-Count, X-Request-ID, X-Response-Time");
        
        // 允许携带认证信息
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // 预检请求缓存时间
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // 添加一些有用的响应头
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
    }
    
    /**
     * 处理OPTIONS预检请求
     */
    private void handlePreflightRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String requestMethod = request.getHeader("Access-Control-Request-Method");
        String requestHeaders = request.getHeader("Access-Control-Request-Headers");
        String origin = request.getHeader("Origin");
        
        log.info("处理OPTIONS预检请求 - Origin: {}, Method: {}, Headers: {}", 
                origin, requestMethod, requestHeaders);
        
        // 验证请求方法
        if (requestMethod != null && isAllowedMethod(requestMethod)) {
            response.setStatus(HttpServletResponse.SC_OK);
            log.debug("预检请求验证通过 - Method: {}", requestMethod);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("预检请求方法不被允许 - Method: {}", requestMethod);
            return;
        }
        
        // 验证请求头
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            log.debug("预检请求包含自定义头 - Headers: {}", requestHeaders);
        }
        
        // 设置响应内容类型
        response.setContentType("text/plain");
        response.setContentLength(0);
        
        // 刷新响应
        response.flushBuffer();
    }
    
    /**
     * 检查HTTP方法是否被允许
     */
    private boolean isAllowedMethod(String method) {
        if (method == null) {
            return false;
        }
        
        String upperMethod = method.toUpperCase();
        return "GET".equals(upperMethod) || 
               "POST".equals(upperMethod) || 
               "PUT".equals(upperMethod) || 
               "DELETE".equals(upperMethod) || 
               "OPTIONS".equals(upperMethod) || 
               "HEAD".equals(upperMethod) || 
               "PATCH".equals(upperMethod);
    }
    
    @Override
    public void destroy() {
        log.info("销毁CORS预检请求过滤器");
    }
}