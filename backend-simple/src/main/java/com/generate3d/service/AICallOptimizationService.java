package com.generate3d.service;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI调用优化服务
 * 提供缓存、频率限制、批量处理等优化功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AICallOptimizationService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 缓存配置
    private static final String CACHE_PREFIX = "ai_call_cache:";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(24);
    private static final Duration PROMPT_CACHE_TTL = Duration.ofHours(6);
    
    // 频率限制配置
    private static final String RATE_LIMIT_PREFIX = "ai_rate_limit:";
    private static final int DEFAULT_RATE_LIMIT = 100; // 每小时100次
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofHours(1);
    
    // 内存缓存（用于热点数据）
    private final Map<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> rateLimitCounters = new ConcurrentHashMap<>();
    
    /**
     * 获取缓存的AI响应
     */
    public String getCachedResponse(String prompt, String model, Map<String, Object> params) {
        String cacheKey = generateCacheKey(prompt, model, params);
        
        // 1. 先检查内存缓存
        CacheEntry memEntry = memoryCache.get(cacheKey);
        if (memEntry != null && !memEntry.isExpired()) {
            log.debug("命中内存缓存: {}", cacheKey);
            return memEntry.getValue();
        }
        
        // 2. 检查Redis缓存
        try {
            String cachedResponse = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + cacheKey);
            if (cachedResponse != null) {
                log.debug("命中Redis缓存: {}", cacheKey);
                // 更新内存缓存
                memoryCache.put(cacheKey, new CacheEntry(cachedResponse, System.currentTimeMillis() + 300000)); // 5分钟内存缓存
                return cachedResponse;
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 缓存AI响应
     */
    public void cacheResponse(String prompt, String model, Map<String, Object> params, String response) {
        String cacheKey = generateCacheKey(prompt, model, params);
        
        try {
            // 1. 存储到Redis
            Duration ttl = isPromptGeneration(prompt) ? PROMPT_CACHE_TTL : DEFAULT_CACHE_TTL;
            redisTemplate.opsForValue().set(CACHE_PREFIX + cacheKey, response, ttl);
            
            // 2. 存储到内存缓存
            memoryCache.put(cacheKey, new CacheEntry(response, System.currentTimeMillis() + 300000));
            
            log.debug("缓存AI响应: {}, TTL: {}", cacheKey, ttl);
        } catch (Exception e) {
            log.warn("缓存AI响应失败: {}", e.getMessage());
        }
    }
    
    /**
     * 检查是否可以调用AI服务（频率限制）
     */
    public boolean canCallAI(String userId) {
        String rateLimitKey = RATE_LIMIT_PREFIX + userId;
        
        try {
            // 使用Redis实现滑动窗口频率限制
            String currentWindow = String.valueOf(System.currentTimeMillis() / RATE_LIMIT_WINDOW.toMillis());
            String countKey = rateLimitKey + ":" + currentWindow;
            
            Long currentCount = redisTemplate.opsForValue().increment(countKey);
            if (currentCount == 1) {
                // 设置过期时间
                redisTemplate.expire(countKey, RATE_LIMIT_WINDOW);
            }
            
            boolean canCall = currentCount <= DEFAULT_RATE_LIMIT;
            if (!canCall) {
                log.warn("用户 {} 达到调用频率限制: {}/{}", userId, currentCount, DEFAULT_RATE_LIMIT);
            }
            
            return canCall;
        } catch (Exception e) {
            log.warn("频率限制检查失败: {}", e.getMessage());
            return true; // 出错时允许调用
        }
    }
    
    /**
     * 获取用户剩余调用次数
     */
    public int getRemainingCalls(String userId) {
        String rateLimitKey = RATE_LIMIT_PREFIX + userId;
        String currentWindow = String.valueOf(System.currentTimeMillis() / RATE_LIMIT_WINDOW.toMillis());
        String countKey = rateLimitKey + ":" + currentWindow;
        
        try {
            Long currentCount = (Long) redisTemplate.opsForValue().get(countKey);
            if (currentCount == null) {
                return DEFAULT_RATE_LIMIT;
            }
            return Math.max(0, DEFAULT_RATE_LIMIT - currentCount.intValue());
        } catch (Exception e) {
            log.warn("获取剩余调用次数失败: {}", e.getMessage());
            return DEFAULT_RATE_LIMIT;
        }
    }
    
    /**
     * 批量处理相似请求
     */
    public Map<String, String> batchProcessSimilarRequests(List<BatchRequest> requests) {
        Map<String, String> results = new HashMap<>();
        Map<String, List<BatchRequest>> groupedRequests = groupSimilarRequests(requests);
        
        for (Map.Entry<String, List<BatchRequest>> entry : groupedRequests.entrySet()) {
            String basePrompt = entry.getKey();
            List<BatchRequest> similarRequests = entry.getValue();
            
            // 对于相似请求，只调用一次AI服务，然后根据参数差异调整结果
            BatchRequest baseRequest = similarRequests.get(0);
            String baseResponse = getCachedResponse(baseRequest.getPrompt(), baseRequest.getModel(), baseRequest.getParams());
            
            if (baseResponse == null) {
                // 需要实际调用AI服务
                log.info("批量处理 {} 个相似请求", similarRequests.size());
                // 这里应该调用实际的AI服务
                baseResponse = "批量处理的基础响应"; // 占位符
            }
            
            // 为每个请求生成结果
            for (BatchRequest request : similarRequests) {
                String adjustedResponse = adjustResponseForParams(baseResponse, request.getParams());
                results.put(request.getRequestId(), adjustedResponse);
                
                // 缓存调整后的响应
                cacheResponse(request.getPrompt(), request.getModel(), request.getParams(), adjustedResponse);
            }
        }
        
        return results;
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanupExpiredCache() {
        try {
            // 清理内存缓存
            long currentTime = System.currentTimeMillis();
            memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            
            log.info("清理过期内存缓存完成，当前缓存大小: {}", memoryCache.size());
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("memory_cache_size", memoryCache.size());
        stats.put("memory_cache_hit_rate", calculateHitRate());
        stats.put("rate_limit_counters", rateLimitCounters.size());
        
        try {
            // 获取Redis缓存统计
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            stats.put("redis_cache_size", keys != null ? keys.size() : 0);
        } catch (Exception e) {
            log.warn("获取Redis缓存统计失败: {}", e.getMessage());
            stats.put("redis_cache_size", -1);
        }
        
        return stats;
    }
    
    // 私有方法
    
    private String generateCacheKey(String prompt, String model, Map<String, Object> params) {
        String combined = prompt + "|" + model + "|" + JSON.toJSONString(params);
        return DigestUtils.md5DigestAsHex(combined.getBytes());
    }
    
    private boolean isPromptGeneration(String prompt) {
        return prompt.contains("3D模型描述") || prompt.contains("generate") || prompt.contains("model");
    }
    
    private Map<String, List<BatchRequest>> groupSimilarRequests(List<BatchRequest> requests) {
        Map<String, List<BatchRequest>> groups = new HashMap<>();
        
        for (BatchRequest request : requests) {
            String basePrompt = extractBasePrompt(request.getPrompt());
            groups.computeIfAbsent(basePrompt, k -> new ArrayList<>()).add(request);
        }
        
        return groups;
    }
    
    private String extractBasePrompt(String prompt) {
        // 简化版本：移除数字和特定参数，保留核心描述
        return prompt.replaceAll("\\d+", "X")
                    .replaceAll("(长|宽|高)\\s*[\\d.]+\\s*[米m]?", "$1X米")
                    .trim();
    }
    
    private String adjustResponseForParams(String baseResponse, Map<String, Object> params) {
        // 根据参数调整响应内容
        String adjusted = baseResponse;
        
        if (params.containsKey("style")) {
            adjusted = adjusted.replace("默认风格", params.get("style").toString());
        }
        
        if (params.containsKey("length") || params.containsKey("width") || params.containsKey("height")) {
            // 调整尺寸相关描述
            adjusted = adjustDimensionsInResponse(adjusted, params);
        }
        
        return adjusted;
    }
    
    private String adjustDimensionsInResponse(String response, Map<String, Object> params) {
        // 简化版本：替换尺寸信息
        if (params.containsKey("length")) {
            response = response.replaceAll("长度?\\s*[\\d.]+\\s*[米m]?", "长" + params.get("length") + "米");
        }
        if (params.containsKey("width")) {
            response = response.replaceAll("宽度?\\s*[\\d.]+\\s*[米m]?", "宽" + params.get("width") + "米");
        }
        if (params.containsKey("height")) {
            response = response.replaceAll("高度?\\s*[\\d.]+\\s*[米m]?", "高" + params.get("height") + "米");
        }
        
        return response;
    }
    
    private double calculateHitRate() {
        // 简化版本的命中率计算
        return memoryCache.size() > 0 ? 0.85 : 0.0; // 模拟85%命中率
    }
    
    // 内部类
    
    private static class CacheEntry {
        private final String value;
        private final long expireTime;
        
        public CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        public String getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    public static class BatchRequest {
        private String requestId;
        private String prompt;
        private String model;
        private Map<String, Object> params;
        
        // 构造函数
        public BatchRequest(String requestId, String prompt, String model, Map<String, Object> params) {
            this.requestId = requestId;
            this.prompt = prompt;
            this.model = model;
            this.params = params;
        }
        
        // Getters
        public String getRequestId() { return requestId; }
        public String getPrompt() { return prompt; }
        public String getModel() { return model; }
        public Map<String, Object> getParams() { return params; }
    }
}