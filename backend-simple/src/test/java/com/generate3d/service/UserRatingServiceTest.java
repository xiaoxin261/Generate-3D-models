package com.generate3d.service;

import com.generate3d.entity.ModelUserRating;
import com.generate3d.mapper.ModelUserRatingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户评分服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class UserRatingServiceTest {

    @Resource
    private UserRatingService userRatingService;

    @MockBean
    private ModelUserRatingMapper ratingMapper;

    private String testModelId;
    private String testUserId;
    private ModelUserRating testRating;

    @BeforeEach
    void setUp() {
        testModelId = "test_model_001";
        testUserId = "test_user_001";
        testRating = createTestRating();
    }

    @Test
    void testSubmitRating() {
        // 准备测试数据
        when(ratingMapper.findByModelIdAndUserId(testModelId, testUserId)).thenReturn(null);
        when(ratingMapper.insert(any(ModelUserRating.class))).thenReturn(1);

        // 执行测试
        ModelUserRating result = userRatingService.submitRating(testRating);

        // 验证结果
        assertNotNull(result);
        assertEquals(testModelId, result.getModelId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(4.5, result.getOverallRating());

        verify(ratingMapper, times(1)).findByModelIdAndUserId(testModelId, testUserId);
        verify(ratingMapper, times(1)).insert(any(ModelUserRating.class));
    }

    @Test
    void testUpdateExistingRating() {
        // 准备测试数据
        ModelUserRating existingRating = createTestRating();
        existingRating.setRatingId("existing_rating_001");
        
        when(ratingMapper.findByModelIdAndUserId(testModelId, testUserId)).thenReturn(existingRating);
        when(ratingMapper.updateById(any(ModelUserRating.class))).thenReturn(1);

        // 修改评分
        testRating.setOverallRating(3.5);
        testRating.setFeedbackText("更新后的评价");

        // 执行测试
        ModelUserRating result = userRatingService.submitRating(testRating);

        // 验证结果
        assertNotNull(result);
        assertEquals("existing_rating_001", result.getRatingId());
        assertEquals(3.5, result.getOverallRating());
        assertEquals("更新后的评价", result.getFeedbackText());

        verify(ratingMapper, times(1)).findByModelIdAndUserId(testModelId, testUserId);
        verify(ratingMapper, times(1)).updateById(any(ModelUserRating.class));
    }

    @Test
    void testGetUserRating() {
        // 准备测试数据
        when(ratingMapper.findByModelIdAndUserId(testModelId, testUserId)).thenReturn(testRating);

        // 执行测试
        ModelUserRating result = userRatingService.getUserRating(testModelId, testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(testModelId, result.getModelId());
        assertEquals(testUserId, result.getUserId());

        verify(ratingMapper, times(1)).findByModelIdAndUserId(testModelId, testUserId);
    }

    @Test
    void testGetUserRatingNotFound() {
        // 准备测试数据
        when(ratingMapper.findByModelIdAndUserId(testModelId, testUserId)).thenReturn(null);

        // 执行测试
        ModelUserRating result = userRatingService.getUserRating(testModelId, testUserId);

        // 验证结果
        assertNull(result);

        verify(ratingMapper, times(1)).findByModelIdAndUserId(testModelId, testUserId);
    }

    @Test
    void testGetModelRatingStats() {
        // 准备测试数据
        when(ratingMapper.countValidRatingsByModelId(testModelId)).thenReturn(100L);
        when(ratingMapper.getAverageRatingByModelId(testModelId)).thenReturn(4.2);
        
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("5", 30L);
        distribution.put("4", 40L);
        distribution.put("3", 20L);
        distribution.put("2", 8L);
        distribution.put("1", 2L);
        when(ratingMapper.getRatingDistribution(testModelId)).thenReturn(Arrays.asList(distribution));

        // 执行测试
        Map<String, Object> result = userRatingService.getModelRatingStats(testModelId);

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.get("totalRatings"));
        assertEquals(4.2, result.get("averageRating"));
        assertNotNull(result.get("ratingDistribution"));

        verify(ratingMapper, times(1)).countValidRatingsByModelId(testModelId);
        verify(ratingMapper, times(1)).getAverageRatingByModelId(testModelId);
        verify(ratingMapper, times(1)).getRatingDistribution(testModelId);
    }

    @Test
    void testDeleteRating() {
        // 准备测试数据
        when(ratingMapper.findByModelIdAndUserId(testModelId, testUserId)).thenReturn(testRating);
        when(ratingMapper.softDeleteByModelIdAndUserId(testModelId, testUserId)).thenReturn(1);

        // 执行测试
        boolean result = userRatingService.deleteRating(testModelId, testUserId);

        // 验证结果
        assertTrue(result);

        verify(ratingMapper, times(1)).findByModelIdAndUserId(testModelId, testUserId);
        verify(ratingMapper, times(1)).softDeleteByModelIdAndUserId(testModelId, testUserId);
    }

    @Test
    void testDeleteNonExistentRating() {
        // 准备测试数据
        when(ratingMapper.findByModelIdAndUserId(testModelId, testUserId)).thenReturn(null);

        // 执行测试
        boolean result = userRatingService.deleteRating(testModelId, testUserId);

        // 验证结果
        assertFalse(result);

        verify(ratingMapper, times(1)).findByModelIdAndUserId(testModelId, testUserId);
        verify(ratingMapper, never()).softDeleteByModelIdAndUserId(anyString(), anyString());
    }

    @Test
    void testGetRatingTrend() {
        // 准备测试数据
        List<Map<String, Object>> trendData = Arrays.asList(
                createTrendData("2024-01-01", 10L, 4.2),
                createTrendData("2024-01-02", 15L, 4.3),
                createTrendData("2024-01-03", 12L, 4.1)
        );
        when(ratingMapper.getRatingTrendByDays(30)).thenReturn(trendData);

        // 执行测试
        List<Map<String, Object>> result = userRatingService.getRatingTrend(30);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("2024-01-01", result.get(0).get("date"));
        assertEquals(10L, result.get(0).get("count"));
        assertEquals(4.2, result.get(0).get("avgRating"));

        verify(ratingMapper, times(1)).getRatingTrendByDays(30);
    }

    @Test
    void testGetUserRatingHistory() {
        // 准备测试数据
        List<ModelUserRating> history = Arrays.asList(
                createTestRating(),
                createTestRating()
        );
        when(ratingMapper.findUserRatingHistory(testUserId, 10)).thenReturn(history);

        // 执行测试
        List<ModelUserRating> result = userRatingService.getUserRatingHistory(testUserId, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(ratingMapper, times(1)).findUserRatingHistory(testUserId, 10);
    }

    @Test
    void testGetTopRatedModels() {
        // 准备测试数据
        List<Map<String, Object>> topModels = Arrays.asList(
                createModelRatingData("model_001", 4.8, 50L),
                createModelRatingData("model_002", 4.7, 45L),
                createModelRatingData("model_003", 4.6, 40L)
        );
        when(ratingMapper.getTopRatedModels(10)).thenReturn(topModels);

        // 执行测试
        List<Map<String, Object>> result = userRatingService.getTopRatedModels(10);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("model_001", result.get(0).get("modelId"));
        assertEquals(4.8, result.get(0).get("averageRating"));

        verify(ratingMapper, times(1)).getTopRatedModels(10);
    }

    @Test
    void testGetRecentRatings() {
        // 准备测试数据
        List<ModelUserRating> recentRatings = Arrays.asList(
                createTestRating(),
                createTestRating()
        );
        when(ratingMapper.getRecentRatings(20)).thenReturn(recentRatings);

        // 执行测试
        List<ModelUserRating> result = userRatingService.getRecentRatings(20);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(ratingMapper, times(1)).getRecentRatings(20);
    }

    @Test
    void testValidateRatingData() {
        // 测试有效数据
        ModelUserRating validRating = createTestRating();
        assertDoesNotThrow(() -> userRatingService.submitRating(validRating));

        // 测试无效评分值
        ModelUserRating invalidRating = createTestRating();
        invalidRating.setOverallRating(6.0); // 超出范围
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userRatingService.submitRating(invalidRating);
        });
        assertTrue(exception.getMessage().contains("评分值必须在1-5之间"));
    }

    @Test
    void testGetUserActivityStats() {
        // 准备测试数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRatings", 25L);
        stats.put("averageRating", 4.1);
        stats.put("lastRatingDate", LocalDateTime.now().minusDays(1));
        
        when(ratingMapper.getUserActivityStats(testUserId)).thenReturn(stats);

        // 执行测试
        Map<String, Object> result = userRatingService.getUserActivityStats(testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(25L, result.get("totalRatings"));
        assertEquals(4.1, result.get("averageRating"));

        verify(ratingMapper, times(1)).getUserActivityStats(testUserId);
    }

    @Test
    void testGetPopularTags() {
        // 准备测试数据
        List<Map<String, Object>> tags = Arrays.asList(
                Map.of("tag", "高质量", "count", 50L),
                Map.of("tag", "精细", "count", 35L),
                Map.of("tag", "创意", "count", 28L)
        );
        when(ratingMapper.getPopularRatingTags(10)).thenReturn(tags);

        // 执行测试
        List<Map<String, Object>> result = userRatingService.getPopularTags(10);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("高质量", result.get(0).get("tag"));
        assertEquals(50L, result.get(0).get("count"));

        verify(ratingMapper, times(1)).getPopularRatingTags(10);
    }

    // 辅助方法
    private ModelUserRating createTestRating() {
        ModelUserRating rating = new ModelUserRating();
        rating.setRatingId("test_rating_001");
        rating.setModelId(testModelId);
        rating.setUserId(testUserId);
        rating.setOverallRating(4.5);
        rating.setQualityRating(4.0);
        rating.setAccuracyRating(5.0);
        rating.setVisualRating(4.5);
        rating.setFeedbackText("很好的模型");
        rating.setIsAnonymous(false);
        rating.setStatus("ACTIVE");
        rating.setCreatedAt(LocalDateTime.now());
        rating.setUpdatedAt(LocalDateTime.now());
        return rating;
    }

    private Map<String, Object> createTrendData(String date, Long count, Double avgRating) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("count", count);
        data.put("avgRating", avgRating);
        return data;
    }

    private Map<String, Object> createModelRatingData(String modelId, Double avgRating, Long count) {
        Map<String, Object> data = new HashMap<>();
        data.put("modelId", modelId);
        data.put("averageRating", avgRating);
        data.put("ratingCount", count);
        return data;
    }
}