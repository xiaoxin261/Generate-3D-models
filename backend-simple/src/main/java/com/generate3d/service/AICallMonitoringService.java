package com.generate3d.service;

import com.generate3d.config.AIOptimizationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI调用监控服务
 * 负责收集、分析和报告AI服务调用的性能指标
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AICallMonitoringService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AIOptimizationConfig config;

    // 内存中的实时统计
    private final Map<String, AtomicInteger> callCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> responseTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> cacheMisses = new ConcurrentHashMap<>();

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 记录AI调用
     */
    @Async
    public void recordAICall(String userId, String operation, long responseTimeMs, boolean success, boolean cacheHit) {
        if (!config.getMonitoring().isEnabled()) {
            return;
        }

        try {
            String hourKey = getCurrentHourKey();
            String dayKey = getCurrentDayKey();
            
            // 更新内存统计
            updateMemoryStats(userId, operation, responseTimeMs, success, cacheHit);
            
            // 更新Redis统计
            updateRedisStats(userId, operation, hourKey, dayKey, responseTimeMs, success, cacheHit);
            
            // 记录详细日志
            if (config.getMonitoring().isDetailedLogging()) {
                log.info("AI调用记录 - 用户:{}, 操作:{}, 响应时间:{}ms, 成功:{}, 缓存命中:{}", 
                    userId, operation, responseTimeMs, success, cacheHit);
            }
            
            // 检查慢查询
            if (responseTimeMs > config.getMonitoring().getSlowQueryThresholdMs()) {
                log.warn("检测到慢查询 - 用户:{}, 操作:{}, 响应时间:{}ms", userId, operation, responseTimeMs);
            }
            
        } catch (Exception e) {
            log.error("记录AI调用统计失败", e);
        }
    }

    /**
     * 更新内存统计
     */
    private void updateMemoryStats(String userId, String operation, long responseTimeMs, boolean success, boolean cacheHit) {
        String key = userId + ":" + operation;
        
        callCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        responseTimes.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(responseTimeMs);
        
        if (!success) {
            errorCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
        if (cacheHit) {
            cacheHits.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        } else {
            cacheMisses.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    /**
     * 更新Redis统计
     */
    private void updateRedisStats(String userId, String operation, String hourKey, String dayKey, 
                                 long responseTimeMs, boolean success, boolean cacheHit) {
        String statsPrefix = config.getStatsKeyPrefix();
        
        // 小时级统计
        String hourStatsKey = statsPrefix + "hour:" + hourKey + ":" + userId + ":" + operation;
        redisTemplate.opsForHash().increment(hourStatsKey, "call_count", 1);
        redisTemplate.opsForHash().increment(hourStatsKey, "total_response_time", responseTimeMs);
        redisTemplate.expire(hourStatsKey, config.getMonitoring().getStatsRetentionHours(), TimeUnit.HOURS);
        
        if (!success) {
            redisTemplate.opsForHash().increment(hourStatsKey, "error_count", 1);
        }
        
        if (cacheHit) {
            redisTemplate.opsForHash().increment(hourStatsKey, "cache_hits", 1);
        } else {
            redisTemplate.opsForHash().increment(hourStatsKey, "cache_misses", 1);
        }
        
        // 日级统计
        String dayStatsKey = statsPrefix + "day:" + dayKey + ":" + userId;
        redisTemplate.opsForHash().increment(dayStatsKey, "total_calls", 1);
        redisTemplate.opsForHash().increment(dayStatsKey, "total_response_time", responseTimeMs);
        redisTemplate.expire(dayStatsKey, config.getMonitoring().getStatsRetentionHours(), TimeUnit.HOURS);
        
        // 全局统计
        String globalStatsKey = statsPrefix + "global:" + dayKey;
        redisTemplate.opsForHash().increment(globalStatsKey, "total_calls", 1);
        redisTemplate.opsForHash().increment(globalStatsKey, "unique_users", userId.hashCode() % 1000); // 简化的用户计数
        redisTemplate.expire(globalStatsKey, config.getMonitoring().getStatsRetentionHours(), TimeUnit.HOURS);
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 从内存获取实时统计
            Map<String, Object> memoryStats = getMemoryStats(userId);
            stats.putAll(memoryStats);
            
            // 从Redis获取历史统计
            Map<String, Object> redisStats = getRedisStats(userId);
            stats.putAll(redisStats);
            
            // 计算衍生指标
            calculateDerivedMetrics(stats);
            
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            stats.put("error", "获取统计信息失败");
        }
        
        return stats;
    }

    /**
     * 获取内存统计
     */
    private Map<String, Object> getMemoryStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        int totalCalls = 0;
        long totalResponseTime = 0;
        int totalErrors = 0;
        int totalCacheHits = 0;
        int totalCacheMisses = 0;
        
        for (Map.Entry<String, AtomicInteger> entry : callCounts.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                totalCalls += entry.getValue().get();
            }
        }
        
        for (Map.Entry<String, AtomicLong> entry : responseTimes.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                totalResponseTime += entry.getValue().get();
            }
        }
        
        for (Map.Entry<String, AtomicInteger> entry : errorCounts.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                totalErrors += entry.getValue().get();
            }
        }
        
        for (Map.Entry<String, AtomicInteger> entry : cacheHits.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                totalCacheHits += entry.getValue().get();
            }
        }
        
        for (Map.Entry<String, AtomicInteger> entry : cacheMisses.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                totalCacheMisses += entry.getValue().get();
            }
        }
        
        stats.put("memory_total_calls", totalCalls);
        stats.put("memory_total_response_time", totalResponseTime);
        stats.put("memory_total_errors", totalErrors);
        stats.put("memory_cache_hits", totalCacheHits);
        stats.put("memory_cache_misses", totalCacheMisses);
        
        return stats;
    }

    /**
     * 获取Redis统计
     */
    private Map<String, Object> getRedisStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String dayKey = getCurrentDayKey();
            String dayStatsKey = config.getStatsKeyPrefix() + "day:" + dayKey + ":" + userId;
            
            Map<Object, Object> dayStats = redisTemplate.opsForHash().entries(dayStatsKey);
            if (!dayStats.isEmpty()) {
                // 安全地转换类型
                Object totalCalls = dayStats.get("total_calls");
                Object totalResponseTime = dayStats.get("total_response_time");
                
                stats.put("today_total_calls", totalCalls != null ? totalCalls : 0);
                stats.put("today_total_response_time", totalResponseTime != null ? totalResponseTime : 0);
            }
            
            // 获取最近24小时的统计
            stats.put("last_24h_stats", getLast24HourStats(userId));
            
        } catch (Exception e) {
            log.error("获取Redis统计失败", e);
        }
        
        return stats;
    }

    /**
     * 获取最近24小时统计
     */
    private List<Map<String, Object>> getLast24HourStats(String userId) {
        List<Map<String, Object>> hourlyStats = new ArrayList<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            for (int i = 23; i >= 0; i--) {
                LocalDateTime hour = now.minusHours(i);
                String hourKey = hour.format(HOUR_FORMATTER);
                
                Map<String, Object> hourStat = new HashMap<>();
                hourStat.put("hour", hourKey);
                
                // 获取该小时的所有操作统计
                Set<String> keys = redisTemplate.keys(config.getStatsKeyPrefix() + "hour:" + hourKey + ":" + userId + ":*");
                
                int totalCalls = 0;
                long totalResponseTime = 0;
                int totalErrors = 0;
                int totalCacheHits = 0;
                int totalCacheMisses = 0;
                
                if (keys != null) {
                    for (String key : keys) {
                        Map<Object, Object> statsMap = redisTemplate.opsForHash().entries(key);
                        // 安全地转换类型
                        totalCalls += parseIntSafely(statsMap.get("call_count"));
                        totalResponseTime += parseLongSafely(statsMap.get("total_response_time"));
                        totalErrors += parseIntSafely(statsMap.get("error_count"));
                        totalCacheHits += parseIntSafely(statsMap.get("cache_hits"));
                        totalCacheMisses += parseIntSafely(statsMap.get("cache_misses"));
                    }
                }
                
                hourStat.put("calls", totalCalls);
                hourStat.put("avg_response_time", totalCalls > 0 ? totalResponseTime / totalCalls : 0);
                hourStat.put("errors", totalErrors);
                hourStat.put("cache_hits", totalCacheHits);
                hourStat.put("cache_misses", totalCacheMisses);
                
                hourlyStats.add(hourStat);
            }
        } catch (Exception e) {
            log.error("获取24小时统计失败", e);
        }
        
        return hourlyStats;
    }

    /**
     * 计算衍生指标
     */
    private void calculateDerivedMetrics(Map<String, Object> stats) {
        try {
            // 计算平均响应时间
            int totalCalls = (Integer) stats.getOrDefault("memory_total_calls", 0);
            long totalResponseTime = (Long) stats.getOrDefault("memory_total_response_time", 0L);
            
            if (totalCalls > 0) {
                stats.put("avg_response_time", totalResponseTime / totalCalls);
            }
            
            // 计算错误率
            int totalErrors = (Integer) stats.getOrDefault("memory_total_errors", 0);
            if (totalCalls > 0) {
                stats.put("error_rate", (double) totalErrors / totalCalls);
            }
            
            // 计算缓存命中率
            int cacheHits = (Integer) stats.getOrDefault("memory_cache_hits", 0);
            int cacheMisses = (Integer) stats.getOrDefault("memory_cache_misses", 0);
            int totalCacheRequests = cacheHits + cacheMisses;
            
            if (totalCacheRequests > 0) {
                stats.put("memory_cache_hit_rate", (double) cacheHits / totalCacheRequests);
            }
            
            // 添加时间戳
            stats.put("generated_at", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.error("计算衍生指标失败", e);
        }
    }

    /**
     * 获取全局统计
     */
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String dayKey = getCurrentDayKey();
            String globalStatsKey = config.getStatsKeyPrefix() + "global:" + dayKey;
            
            Map<Object, Object> globalStats = redisTemplate.opsForHash().entries(globalStatsKey);
            // 安全地转换Map<Object,Object>到Map<String,Object>
            for (Map.Entry<Object, Object> entry : globalStats.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    stats.put(entry.getKey().toString(), entry.getValue());
                }
            }
            
            // 添加系统状态
            stats.put("optimization_enabled", config.isOptimizationEnabled());
            stats.put("monitoring_enabled", config.getMonitoring().isEnabled());
            stats.put("config_summary", config.getConfigSummary());
            
        } catch (Exception e) {
            log.error("获取全局统计失败", e);
        }
        
        return stats;
    }

    /**
     * 安全地解析整数值
     */
    private int parseIntSafely(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 安全地解析长整数值
     */
    private long parseLongSafely(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            if (value instanceof Long) {
                return (Long) value;
            }
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 定期清理过期统计数据
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredStats() {
        if (!config.getMonitoring().isEnabled()) {
            return;
        }
        
        try {
            log.info("开始清理过期统计数据");
            
            // 清理内存统计（保留最近1小时的数据）
            cleanupMemoryStats();
            
            // Redis数据通过TTL自动过期，无需手动清理
            
            log.info("统计数据清理完成");
            
        } catch (Exception e) {
            log.error("清理统计数据失败", e);
        }
    }

    /**
     * 清理内存统计
     */
    private void cleanupMemoryStats() {
        // 简单的清理策略：每小时重置内存统计
        // 在生产环境中，可以实现更复杂的清理逻辑
        int beforeSize = callCounts.size();
        
        callCounts.clear();
        responseTimes.clear();
        errorCounts.clear();
        cacheHits.clear();
        cacheMisses.clear();
        
        log.info("清理内存统计完成，清理前条目数: {}", beforeSize);
    }

    private String getCurrentHourKey() {
        return LocalDateTime.now().format(HOUR_FORMATTER);
    }

    private String getCurrentDayKey() {
        return LocalDateTime.now().format(DAY_FORMATTER);
    }
}