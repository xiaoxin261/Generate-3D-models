package com.generate3d.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Redis缓存配置
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {
    
    /**
     * RedisTemplate配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用String序列化器作为key的序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        
        // 使用FastJson2序列化器作为value的序列化器
        FastJson2RedisSerializer<Object> fastJson2RedisSerializer = new FastJson2RedisSerializer<>(Object.class);
        template.setValueSerializer(fastJson2RedisSerializer);
        template.setHashValueSerializer(fastJson2RedisSerializer);
        
        template.afterPropertiesSet();
        
        log.info("RedisTemplate配置完成");
        return template;
    }
    
    /**
     * 缓存管理器配置
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认1小时过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new FastJson2RedisSerializer<>(Object.class)))
                .disableCachingNullValues(); // 不缓存null值
        
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // 任务状态缓存：30分钟过期
                .withCacheConfiguration("taskStatus", 
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                // 模型信息缓存：2小时过期
                .withCacheConfiguration("modelInfo", 
                        defaultConfig.entryTtl(Duration.ofHours(2)))
                // 用户收藏缓存：1小时过期
                .withCacheConfiguration("userFavorites", 
                        defaultConfig.entryTtl(Duration.ofHours(1)))
                // 系统配置缓存：24小时过期
                .withCacheConfiguration("systemConfig", 
                        defaultConfig.entryTtl(Duration.ofHours(24)))
                // 文件信息缓存：6小时过期
                .withCacheConfiguration("fileInfo", 
                        defaultConfig.entryTtl(Duration.ofHours(6)))
                // 统计数据缓存：15分钟过期
                .withCacheConfiguration("statistics", 
                        defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        RedisCacheManager cacheManager = builder.build();
        
        log.info("Redis缓存管理器配置完成");
        return cacheManager;
    }
    
    /**
     * FastJson2 Redis序列化器
     */
    public static class FastJson2RedisSerializer<T> implements RedisSerializer<T> {
        
        private final Class<T> clazz;
        
        public FastJson2RedisSerializer(Class<T> clazz) {
            this.clazz = clazz;
        }
        
        @Override
        public byte[] serialize(T t) {
            if (t == null) {
                return new byte[0];
            }
            try {
                return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Redis序列化失败", e);
                throw new RuntimeException("Redis序列化失败", e);
            }
        }
        
        @Override
        public T deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                String str = new String(bytes, StandardCharsets.UTF_8);
                return JSON.parseObject(str, clazz, JSONReader.Feature.SupportAutoType);
            } catch (Exception e) {
                log.error("Redis反序列化失败", e);
                throw new RuntimeException("Redis反序列化失败", e);
            }
        }
    }
    
    /**
     * Redis缓存键前缀常量
     */
    public static class CacheKeys {
        public static final String TASK_STATUS_PREFIX = "task:status:";
        public static final String MODEL_INFO_PREFIX = "model:info:";
        public static final String USER_FAVORITES_PREFIX = "user:favorites:";
        public static final String SYSTEM_CONFIG_PREFIX = "system:config:";
        public static final String FILE_INFO_PREFIX = "file:info:";
        public static final String STATISTICS_PREFIX = "statistics:";
        public static final String GENERATION_QUEUE = "queue:generation";
        public static final String EXPORT_QUEUE = "queue:export";
        public static final String TEMP_FILES_PREFIX = "temp:files:";
        
        /**
         * 生成任务状态缓存键
         */
        public static String taskStatusKey(String taskId) {
            return TASK_STATUS_PREFIX + taskId;
        }
        
        /**
         * 生成模型信息缓存键
         */
        public static String modelInfoKey(String modelId) {
            return MODEL_INFO_PREFIX + modelId;
        }
        
        /**
         * 生成用户收藏缓存键
         */
        public static String userFavoritesKey(String userId) {
            return USER_FAVORITES_PREFIX + userId;
        }
        
        /**
         * 生成系统配置缓存键
         */
        public static String systemConfigKey(String configKey) {
            return SYSTEM_CONFIG_PREFIX + configKey;
        }
        
        /**
         * 生成文件信息缓存键
         */
        public static String fileInfoKey(String filePath) {
            return FILE_INFO_PREFIX + filePath.hashCode();
        }
        
        /**
         * 生成统计数据缓存键
         */
        public static String statisticsKey(String type) {
            return STATISTICS_PREFIX + type;
        }
        
        /**
         * 生成临时文件缓存键
         */
        public static String tempFilesKey(String sessionId) {
            return TEMP_FILES_PREFIX + sessionId;
        }
    }
    
    /**
     * 缓存过期时间常量（秒）
     */
    public static class CacheExpire {
        public static final long TASK_STATUS = 30 * 60; // 30分钟
        public static final long MODEL_INFO = 2 * 60 * 60; // 2小时
        public static final long USER_FAVORITES = 60 * 60; // 1小时
        public static final long SYSTEM_CONFIG = 24 * 60 * 60; // 24小时
        public static final long FILE_INFO = 6 * 60 * 60; // 6小时
        public static final long STATISTICS = 15 * 60; // 15分钟
        public static final long TEMP_FILES = 60 * 60; // 1小时
        public static final long SHORT_TERM = 5 * 60; // 5分钟
        public static final long MEDIUM_TERM = 30 * 60; // 30分钟
        public static final long LONG_TERM = 2 * 60 * 60; // 2小时
    }
}