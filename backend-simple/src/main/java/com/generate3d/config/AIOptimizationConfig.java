package com.generate3d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI调用优化配置
 * 定义缓存、频率限制、批量处理等优化参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.optimization")
public class AIOptimizationConfig {

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    /**
     * 频率限制配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 批量处理配置
     */
    private Batch batch = new Batch();

    /**
     * 异步处理配置
     */
    private Async async = new Async();

    /**
     * 监控配置
     */
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Cache {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;

        /**
         * 内存缓存TTL（小时）
         */
        private int memoryTtlHours = 2;

        /**
         * Redis缓存TTL（小时）
         */
        private int redisTtlHours = 24;

        /**
         * 内存缓存最大条目数
         */
        private int memoryMaxEntries = 1000;

        /**
         * 缓存清理间隔（分钟）
         */
        private int cleanupIntervalMinutes = 30;

        /**
         * 预热缓存的常用提示词
         */
        private String[] warmupPrompts = {
            "生成一个现代风格的椅子",
            "创建一个简约的桌子",
            "设计一个科技感的灯具",
            "制作一个卡通角色模型"
        };
    }

    @Data
    public static class RateLimit {
        /**
         * 是否启用频率限制
         */
        private boolean enabled = true;

        /**
         * 每小时最大调用次数
         */
        private int maxCallsPerHour = 100;

        /**
         * 每分钟最大调用次数
         */
        private int maxCallsPerMinute = 10;

        /**
         * 滑动窗口大小（分钟）
         */
        private int windowSizeMinutes = 60;

        /**
         * 超出限制时的等待时间（秒）
         */
        private int waitTimeSeconds = 60;
    }

    @Data
    public static class Batch {
        /**
         * 是否启用批量处理
         */
        private boolean enabled = true;

        /**
         * 批量处理最大大小
         */
        private int maxBatchSize = 10;

        /**
         * 批量等待时间（毫秒）
         */
        private int waitTimeMs = 100;

        /**
         * 相似度阈值（用于合并相似请求）
         */
        private double similarityThreshold = 0.8;
    }

    @Data
    public static class Async {
        /**
         * 是否启用异步处理
         */
        private boolean enabled = true;

        /**
         * 异步线程池核心大小
         */
        private int corePoolSize = 5;

        /**
         * 异步线程池最大大小
         */
        private int maxPoolSize = 20;

        /**
         * 队列容量
         */
        private int queueCapacity = 100;

        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds = 60;

        /**
         * 线程名前缀
         */
        private String threadNamePrefix = "ai-async-";
    }

    @Data
    public static class Monitoring {
        /**
         * 是否启用监控
         */
        private boolean enabled = true;

        /**
         * 统计数据保留时间（小时）
         */
        private int statsRetentionHours = 168; // 7天

        /**
         * 性能指标收集间隔（秒）
         */
        private int metricsIntervalSeconds = 60;

        /**
         * 是否记录详细日志
         */
        private boolean detailedLogging = false;

        /**
         * 慢查询阈值（毫秒）
         */
        private long slowQueryThresholdMs = 5000;
    }

    /**
     * 获取缓存键前缀
     */
    public String getCacheKeyPrefix() {
        return "ai:optimization:";
    }

    /**
     * 获取频率限制键前缀
     */
    public String getRateLimitKeyPrefix() {
        return "ai:rate_limit:";
    }

    /**
     * 获取统计键前缀
     */
    public String getStatsKeyPrefix() {
        return "ai:stats:";
    }

    /**
     * 是否启用所有优化功能
     */
    public boolean isOptimizationEnabled() {
        return cache.enabled || rateLimit.enabled || batch.enabled || async.enabled;
    }

    /**
     * 获取总体配置摘要
     */
    public String getConfigSummary() {
        return String.format(
            "AI优化配置 - 缓存:%s, 频率限制:%s, 批量处理:%s, 异步处理:%s, 监控:%s",
            cache.enabled ? "启用" : "禁用",
            rateLimit.enabled ? "启用" : "禁用",
            batch.enabled ? "启用" : "禁用",
            async.enabled ? "启用" : "禁用",
            monitoring.enabled ? "启用" : "禁用"
        );
    }
}