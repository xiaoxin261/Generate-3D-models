package com.generate3d.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generate3d.entity.EvaluationTask;
import com.generate3d.entity.ModelEvaluation;
import com.generate3d.entity.ModelUserRating;
import com.generate3d.service.ModelEvaluationService;
import com.generate3d.service.UserRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 评估系统端到端集成测试
 * 测试完整的评估流程，包括任务创建、执行、结果获取和用户评分
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class EvaluationSystemIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ModelEvaluationService evaluationService;

    @Resource
    private UserRatingService ratingService;

    private String testModelId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testModelId = "integration_test_model_001";
        testUserId = "integration_test_user_001";
    }

    @Test
    void testCompleteEvaluationWorkflow() throws Exception {
        // 1. 创建评估任务
        String taskId = createEvaluationTask();
        assertNotNull(taskId);

        // 2. 检查任务状态
        EvaluationTask task = evaluationService.getTaskStatus(taskId);
        assertNotNull(task);
        assertEquals("PENDING", task.getStatus());

        // 3. 模拟异步执行评估
        CompletableFuture<Void> evaluationFuture = evaluationService.executeEvaluationAsync(taskId);
        
        // 等待评估完成（最多30秒）
        evaluationFuture.get(30, TimeUnit.SECONDS);

        // 4. 检查评估结果
        ModelEvaluation result = evaluationService.getEvaluationResult(testModelId);
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertTrue(result.getOverallScore() > 0);

        // 5. 提交用户评分
        ModelUserRating rating = createAndSubmitUserRating();
        assertNotNull(rating);
        assertEquals(testModelId, rating.getModelId());

        // 6. 获取模型评分统计
        Map<String, Object> stats = ratingService.getModelRatingStats(testModelId);
        assertNotNull(stats);
        assertTrue((Long) stats.get("totalRatings") > 0);

        // 7. 获取系统概览
        Map<String, Object> overview = evaluationService.getSystemOverview();
        assertNotNull(overview);
        assertNotNull(overview.get("task_stats"));
        assertNotNull(overview.get("evaluation_stats"));
        assertNotNull(overview.get("rating_stats"));
    }

    @Test
    void testEvaluationTaskRetryMechanism() throws Exception {
        // 1. 创建评估任务
        String taskId = createEvaluationTask();

        // 2. 模拟任务失败
        evaluationService.markTaskFailed(taskId, "模拟失败测试");

        // 3. 检查任务状态
        EvaluationTask failedTask = evaluationService.getTaskStatus(taskId);
        assertEquals("FAILED", failedTask.getStatus());
        assertEquals(1, failedTask.getRetryCount());

        // 4. 重试任务
        boolean retryResult = evaluationService.retryTask(taskId);
        assertTrue(retryResult);

        // 5. 检查重试后的状态
        EvaluationTask retriedTask = evaluationService.getTaskStatus(taskId);
        assertEquals("PENDING", retriedTask.getStatus());
        assertEquals(1, retriedTask.getRetryCount());
    }

    @Test
    void testConcurrentEvaluationTasks() throws Exception {
        // 创建多个并发评估任务
        String[] taskIds = new String[5];
        CompletableFuture<String>[] futures = new CompletableFuture[5];

        for (int i = 0; i < 5; i++) {
            final String modelId = testModelId + "_concurrent_" + i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    return evaluationService.createEvaluationTask(modelId, "QUICK", "MEDIUM");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // 等待所有任务创建完成
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

        // 验证所有任务都已创建
        for (int i = 0; i < 5; i++) {
            taskIds[i] = futures[i].get();
            assertNotNull(taskIds[i]);
            
            EvaluationTask task = evaluationService.getTaskStatus(taskIds[i]);
            assertNotNull(task);
            assertEquals("PENDING", task.getStatus());
        }
    }

    @Test
    void testUserRatingWorkflow() throws Exception {
        // 1. 提交初始评分
        ModelUserRating initialRating = createTestRating(4.0, "初始评价");
        ModelUserRating submittedRating = ratingService.submitRating(initialRating);
        assertNotNull(submittedRating);
        assertEquals(4.0, submittedRating.getOverallRating());

        // 2. 更新评分
        ModelUserRating updatedRating = createTestRating(4.5, "更新后的评价");
        ModelUserRating resubmittedRating = ratingService.submitRating(updatedRating);
        assertEquals(4.5, resubmittedRating.getOverallRating());
        assertEquals("更新后的评价", resubmittedRating.getFeedbackText());

        // 3. 获取用户评分
        ModelUserRating retrievedRating = ratingService.getUserRating(testModelId, testUserId);
        assertNotNull(retrievedRating);
        assertEquals(4.5, retrievedRating.getOverallRating());

        // 4. 删除评分
        boolean deleteResult = ratingService.deleteRating(testModelId, testUserId);
        assertTrue(deleteResult);

        // 5. 验证评分已删除
        ModelUserRating deletedRating = ratingService.getUserRating(testModelId, testUserId);
        assertNull(deletedRating);
    }

    @Test
    void testEvaluationHistoryTracking() throws Exception {
        // 1. 创建并完成第一次评估
        String taskId1 = createEvaluationTask();
        completeEvaluationTask(taskId1, 8.0, "GOOD");

        // 2. 创建并完成第二次评估（重新评估）
        String taskId2 = evaluationService.createEvaluationTask(testModelId, "FULL", "HIGH");
        completeEvaluationTask(taskId2, 8.5, "EXCELLENT");

        // 3. 获取评估历史
        var history = evaluationService.getEvaluationHistory(testModelId, 10);
        assertNotNull(history);
        assertTrue(history.size() >= 2);

        // 4. 验证历史记录按时间排序
        LocalDateTime previousTime = LocalDateTime.now().plusDays(1);
        for (ModelEvaluation evaluation : history) {
            assertTrue(evaluation.getEvaluatedAt().isBefore(previousTime));
            previousTime = evaluation.getEvaluatedAt();
        }
    }

    @Test
    void testSystemPerformanceMetrics() throws Exception {
        // 1. 创建多个评估任务和评分
        for (int i = 0; i < 10; i++) {
            String modelId = testModelId + "_perf_" + i;
            String taskId = evaluationService.createEvaluationTask(modelId, "QUICK", "LOW");
            completeEvaluationTask(taskId, 7.0 + (i * 0.1), "GOOD");
            
            // 添加用户评分
            ModelUserRating rating = createTestRating(4.0 + (i * 0.05), "性能测试评分 " + i);
            rating.setModelId(modelId);
            ratingService.submitRating(rating);
        }

        // 2. 获取系统概览
        Map<String, Object> overview = evaluationService.getSystemOverview();
        
        // 3. 验证统计数据
        Map<String, Object> taskStats = (Map<String, Object>) overview.get("task_stats");
        assertTrue((Integer) taskStats.get("total") >= 10);
        
        Map<String, Object> ratingStats = (Map<String, Object>) overview.get("rating_stats");
        assertTrue((Integer) ratingStats.get("total") >= 10);
    }

    @Test
    void testErrorHandlingAndRecovery() throws Exception {
        // 1. 测试无效模型ID
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.createEvaluationTask("", "FULL", "HIGH");
        });
        assertTrue(exception.getMessage().contains("模型ID不能为空"));

        // 2. 测试无效评分值
        ModelUserRating invalidRating = createTestRating(6.0, "无效评分");
        Exception ratingException = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.submitRating(invalidRating);
        });
        assertTrue(ratingException.getMessage().contains("评分值必须在1-5之间"));

        // 3. 测试获取不存在的任务
        EvaluationTask nonExistentTask = evaluationService.getTaskStatus("non_existent_task");
        assertNull(nonExistentTask);
    }

    @Test
    void testDataConsistency() throws Exception {
        // 1. 创建评估任务
        String taskId = createEvaluationTask();
        
        // 2. 完成评估
        completeEvaluationTask(taskId, 8.2, "GOOD");
        
        // 3. 添加多个用户评分
        for (int i = 0; i < 5; i++) {
            ModelUserRating rating = createTestRating(4.0 + (i * 0.2), "用户评分 " + i);
            rating.setUserId(testUserId + "_" + i);
            ratingService.submitRating(rating);
        }
        
        // 4. 验证数据一致性
        ModelEvaluation evaluation = evaluationService.getEvaluationResult(testModelId);
        Map<String, Object> ratingStats = ratingService.getModelRatingStats(testModelId);
        
        assertNotNull(evaluation);
        assertEquals(testModelId, evaluation.getModelId());
        assertEquals(5L, ratingStats.get("totalRatings"));
        
        // 5. 验证平均评分计算正确
        Double expectedAverage = (4.0 + 4.2 + 4.4 + 4.6 + 4.8) / 5.0;
        Double actualAverage = (Double) ratingStats.get("averageRating");
        assertEquals(expectedAverage, actualAverage, 0.01);
    }

    // 辅助方法
    private String createEvaluationTask() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("modelId", testModelId);
        request.put("evaluationType", "FULL");
        request.put("priority", "HIGH");

        String response = mockMvc.perform(post("/v1/evaluation/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        return (String) data.get("taskId");
    }

    private ModelUserRating createAndSubmitUserRating() throws Exception {
        ModelUserRating rating = createTestRating(4.5, "集成测试评分");
        return ratingService.submitRating(rating);
    }

    private ModelUserRating createTestRating(Double overallRating, String feedback) {
        ModelUserRating rating = new ModelUserRating();
        rating.setModelId(testModelId);
        rating.setUserId(testUserId);
        rating.setOverallRating(overallRating);
        rating.setQualityRating(overallRating - 0.1);
        rating.setAccuracyRating(overallRating + 0.1);
        rating.setVisualRating(overallRating);
        rating.setFeedbackText(feedback);
        rating.setIsAnonymous(false);
        rating.setStatus("ACTIVE");
        rating.setCreatedAt(LocalDateTime.now());
        return rating;
    }

    private void completeEvaluationTask(String taskId, Double score, String grade) {
        // 模拟评估任务完成
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(testModelId);
        evaluation.setFinalScore(BigDecimal.valueOf(score)); // 使用setFinalScore而不是setOverallScore
        evaluation.setGeometricScore(BigDecimal.valueOf(score - 0.2));
        evaluation.setVisualScore(BigDecimal.valueOf(score + 0.1));
        evaluation.setTechnicalScore(BigDecimal.valueOf(score - 0.1));
        evaluation.setGrade(grade);
        evaluation.setStatus("COMPLETED");
        evaluation.setCreatedAt(LocalDateTime.now()); // 使用setCreatedAt而不是setEvaluatedAt
        
        // 这里应该调用实际的服务方法来保存评估结果
        // evaluationService.saveEvaluationResult(evaluation);
    }
}