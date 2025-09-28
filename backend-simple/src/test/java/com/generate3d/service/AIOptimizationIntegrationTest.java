package com.generate3d.service;

import com.generate3d.config.AIOptimizationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI优化集成测试
 * 验证缓存、频率限制、批量处理、监控等功能的集成效果
 */
@SpringBootTest
@ActiveProfiles("test")
public class AIOptimizationIntegrationTest {

    @Autowired
    private AICallOptimizationService optimizationService;

    @Autowired
    private OptimizedHunyuanService optimizedHunyuanService;

    @Autowired
    private AICallMonitoringService monitoringService;

    @Autowired
    private AIOptimizationConfig config;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_PROMPT = "生成一个现代风格的椅子";

    @BeforeEach
    void setUp() {
        // 清理测试数据
        cleanupTestData();
        
        // 确保配置启用
        assertTrue(config.getCache().isEnabled(), "缓存应该启用");
        assertTrue(config.getRateLimit().isEnabled(), "频率限制应该启用");
        assertTrue(config.getBatch().isEnabled(), "批量处理应该启用");
        assertTrue(config.getMonitoring().isEnabled(), "监控应该启用");
    }

    @Test
    void testCacheOptimization() throws Exception {
        System.out.println("=== 测试缓存优化效果 ===");
        
        // 第一次调用 - 应该缓存未命中
        long startTime1 = System.currentTimeMillis();
        String result1 = optimizedHunyuanService.optimizedChatCompletion(TEST_PROMPT, TEST_USER_ID);
        long duration1 = System.currentTimeMillis() - startTime1;
        
        assertNotNull(result1, "第一次调用应该返回结果");
        System.out.println("第一次调用耗时: " + duration1 + "ms");
        
        // 等待一小段时间确保缓存生效
        Thread.sleep(100);
        
        // 第二次调用相同内容 - 应该缓存命中
        long startTime2 = System.currentTimeMillis();
        String result2 = optimizedHunyuanService.optimizedChatCompletion(TEST_PROMPT, TEST_USER_ID);
        long duration2 = System.currentTimeMillis() - startTime2;
        
        assertNotNull(result2, "第二次调用应该返回结果");
        assertEquals(result1, result2, "缓存命中时结果应该相同");
        
        // 缓存命中的调用应该明显更快
        assertTrue(duration2 < duration1 / 2, 
            String.format("缓存命中应该更快 - 第一次: %dms, 第二次: %dms", duration1, duration2));
        
        System.out.println("第二次调用耗时: " + duration2 + "ms (缓存命中)");
        
        // 验证缓存统计
        Map<String, Object> cacheStats = optimizationService.getCacheStats();
        assertTrue((Integer) cacheStats.get("memory_hit_count") > 0, "应该有内存缓存命中");
        
        System.out.println("缓存统计: " + cacheStats);
    }

    @Test
    void testRateLimitOptimization() throws Exception {
        System.out.println("=== 测试频率限制优化 ===");
        
        String userId = "rate-limit-test-user";
        int maxCalls = 5; // 设置较小的限制用于测试
        
        // 模拟快速连续调用
        int successCount = 0;
        int rateLimitedCount = 0;
        
        for (int i = 0; i < 10; i++) {
            try {
                if (optimizationService.checkRateLimit(userId)) {
                    String result = optimizedHunyuanService.optimizedChatCompletion(
                        "测试提示词 " + i, userId);
                    if (result != null) {
                        successCount++;
                    }
                } else {
                    rateLimitedCount++;
                }
            } catch (Exception e) {
                if (e.getMessage().contains("频率限制")) {
                    rateLimitedCount++;
                } else {
                    throw e;
                }
            }
            
            Thread.sleep(50); // 短暂间隔
        }
        
        System.out.println("成功调用次数: " + successCount);
        System.out.println("被限制次数: " + rateLimitedCount);
        
        assertTrue(rateLimitedCount > 0, "应该有调用被频率限制");
        assertTrue(successCount > 0, "应该有成功的调用");
        
        // 验证剩余调用次数
        int remaining = optimizationService.getRemainingCalls(userId);
        assertTrue(remaining >= 0, "剩余调用次数应该非负");
        
        System.out.println("剩余调用次数: " + remaining);
    }

    @Test
    void testBatchProcessingOptimization() throws Exception {
        System.out.println("=== 测试批量处理优化 ===");
        
        Map<String, String> batchPrompts = new HashMap<>();
        batchPrompts.put("chair", "生成一个现代风格的椅子");
        batchPrompts.put("table", "创建一个简约的桌子");
        batchPrompts.put("lamp", "设计一个科技感的灯具");
        
        long startTime = System.currentTimeMillis();
        Map<String, String> results = optimizedHunyuanService.batchGenerate3DModelPrompts(
            batchPrompts, TEST_USER_ID);
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(results, "批量处理应该返回结果");
        assertEquals(batchPrompts.size(), results.size(), "结果数量应该匹配");
        
        // 验证所有结果都不为空
        for (Map.Entry<String, String> entry : results.entrySet()) {
            assertNotNull(entry.getValue(), "批量结果不应该为空: " + entry.getKey());
            assertFalse(entry.getValue().trim().isEmpty(), "批量结果不应该为空字符串: " + entry.getKey());
        }
        
        System.out.println("批量处理耗时: " + duration + "ms");
        System.out.println("批量处理结果: " + results);
        
        // 批量处理应该比单独调用更高效
        assertTrue(duration < 5000, "批量处理应该在合理时间内完成");
    }

    @Test
    void testAsyncProcessingOptimization() throws Exception {
        System.out.println("=== 测试异步处理优化 ===");
        
        int concurrentRequests = 3;
        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        CompletableFuture<String>[] futures = new CompletableFuture[concurrentRequests];
        
        long startTime = System.currentTimeMillis();
        
        // 启动并发异步请求
        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            futures[i] = optimizedHunyuanService.asyncChatCompletion(
                "异步测试提示词 " + index, TEST_USER_ID + "-" + index)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        System.out.println("异步请求 " + index + " 完成: " + 
                            (result != null ? result.substring(0, Math.min(50, result.length())) + "..." : "null"));
                    } else {
                        System.err.println("异步请求 " + index + " 失败: " + throwable.getMessage());
                    }
                    latch.countDown();
                });
        }
        
        // 等待所有异步请求完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(completed, "所有异步请求应该在30秒内完成");
        System.out.println("异步处理总耗时: " + duration + "ms");
        
        // 验证所有异步请求的结果
        for (int i = 0; i < concurrentRequests; i++) {
            assertTrue(futures[i].isDone(), "异步请求 " + i + " 应该完成");
            if (!futures[i].isCompletedExceptionally()) {
                String result = futures[i].get();
                assertNotNull(result, "异步请求 " + i + " 应该有结果");
            }
        }
        
        // 异步处理应该比同步处理更高效
        assertTrue(duration < concurrentRequests * 3000, 
            "异步处理应该比同步处理更高效");
    }

    @Test
    void testMonitoringAndStats() throws Exception {
        System.out.println("=== 测试监控和统计功能 ===");
        
        String monitoringUserId = "monitoring-test-user";
        
        // 执行一些AI调用以生成统计数据
        for (int i = 0; i < 5; i++) {
            try {
                optimizedHunyuanService.optimizedChatCompletion(
                    "监控测试提示词 " + i, monitoringUserId);
                Thread.sleep(100);
            } catch (Exception e) {
                // 忽略可能的频率限制错误
            }
        }
        
        // 等待统计数据更新
        Thread.sleep(500);
        
        // 获取用户统计
        Map<String, Object> userStats = monitoringService.getUserStats(monitoringUserId);
        assertNotNull(userStats, "用户统计不应该为空");
        
        System.out.println("用户统计: " + userStats);
        
        // 验证统计数据
        assertTrue(userStats.containsKey("memory_total_calls"), "应该包含总调用次数");
        assertTrue(userStats.containsKey("generated_at"), "应该包含生成时间");
        
        // 获取服务统计
        Map<String, Object> serviceStats = optimizedHunyuanService.getServiceStats(monitoringUserId);
        assertNotNull(serviceStats, "服务统计不应该为空");
        
        System.out.println("服务统计: " + serviceStats);
        
        // 获取全局统计
        Map<String, Object> globalStats = monitoringService.getGlobalStats();
        assertNotNull(globalStats, "全局统计不应该为空");
        
        System.out.println("全局统计: " + globalStats);
        
        // 验证配置信息
        assertTrue(globalStats.containsKey("optimization_enabled"), "应该包含优化启用状态");
        assertTrue(globalStats.containsKey("config_summary"), "应该包含配置摘要");
    }

    @Test
    void testCacheWarmup() throws Exception {
        System.out.println("=== 测试缓存预热功能 ===");
        
        // 执行缓存预热
        optimizedHunyuanService.warmupCache(TEST_USER_ID);
        
        // 等待预热完成
        Thread.sleep(1000);
        
        // 验证预热效果 - 调用预热的提示词应该很快
        String[] warmupPrompts = config.getCache().getWarmupPrompts();
        
        for (String prompt : warmupPrompts) {
            long startTime = System.currentTimeMillis();
            String result = optimizedHunyuanService.optimizedChatCompletion(prompt, TEST_USER_ID);
            long duration = System.currentTimeMillis() - startTime;
            
            assertNotNull(result, "预热的提示词应该有结果");
            assertTrue(duration < 1000, 
                String.format("预热的提示词应该快速响应 - 提示词: %s, 耗时: %dms", 
                    prompt.substring(0, Math.min(20, prompt.length())), duration));
        }
        
        System.out.println("缓存预热验证完成");
    }

    @Test
    void testServiceAvailability() {
        System.out.println("=== 测试服务可用性 ===");
        
        // 验证服务可用性
        assertTrue(optimizedHunyuanService.isServiceAvailable(), "优化服务应该可用");
        
        // 验证配置
        assertTrue(config.isOptimizationEnabled(), "优化功能应该启用");
        
        System.out.println("配置摘要: " + config.getConfigSummary());
        System.out.println("服务可用性验证完成");
    }

    @Test
    void testIntegratedOptimizationWorkflow() throws Exception {
        System.out.println("=== 测试集成优化工作流 ===");
        
        String workflowUserId = "workflow-test-user";
        
        // 1. 预热缓存
        optimizedHunyuanService.warmupCache(workflowUserId);
        Thread.sleep(500);
        
        // 2. 执行批量请求
        Map<String, String> batchPrompts = new HashMap<>();
        batchPrompts.put("modern_chair", "生成一个现代风格的椅子");
        batchPrompts.put("simple_table", "创建一个简约的桌子");
        
        long batchStartTime = System.currentTimeMillis();
        Map<String, String> batchResults = optimizedHunyuanService.batchGenerate3DModelPrompts(
            batchPrompts, workflowUserId);
        long batchDuration = System.currentTimeMillis() - batchStartTime;
        
        // 3. 执行缓存命中测试
        long cacheStartTime = System.currentTimeMillis();
        String cachedResult = optimizedHunyuanService.optimizedChatCompletion(
            "生成一个现代风格的椅子", workflowUserId);
        long cacheDuration = System.currentTimeMillis() - cacheStartTime;
        
        // 4. 验证结果
        assertNotNull(batchResults, "批量结果不应该为空");
        assertEquals(2, batchResults.size(), "应该有2个批量结果");
        assertNotNull(cachedResult, "缓存结果不应该为空");
        
        // 缓存命中应该很快
        assertTrue(cacheDuration < 500, 
            String.format("缓存命中应该很快: %dms", cacheDuration));
        
        // 5. 获取综合统计
        Map<String, Object> finalStats = monitoringService.getUserStats(workflowUserId);
        
        System.out.println("=== 集成优化工作流结果 ===");
        System.out.println("批量处理耗时: " + batchDuration + "ms");
        System.out.println("缓存命中耗时: " + cacheDuration + "ms");
        System.out.println("最终统计: " + finalStats);
        
        // 验证优化效果
        assertTrue(batchDuration < 10000, "批量处理应该在合理时间内完成");
        assertTrue(cacheDuration < batchDuration / 5, "缓存命中应该比批量处理快得多");
        
        System.out.println("集成优化工作流测试完成 ✓");
    }

    private void cleanupTestData() {
        try {
            // 清理Redis测试数据
            String cachePrefix = config.getCacheKeyPrefix();
            String rateLimitPrefix = config.getRateLimitKeyPrefix();
            String statsPrefix = config.getStatsKeyPrefix();
            
            redisTemplate.delete(redisTemplate.keys(cachePrefix + "*"));
            redisTemplate.delete(redisTemplate.keys(rateLimitPrefix + "*"));
            redisTemplate.delete(redisTemplate.keys(statsPrefix + "*"));
            
        } catch (Exception e) {
            System.err.println("清理测试数据失败: " + e.getMessage());
        }
    }
}