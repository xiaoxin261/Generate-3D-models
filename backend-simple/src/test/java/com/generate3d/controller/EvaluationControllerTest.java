package com.generate3d.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generate3d.entity.EvaluationTask;
import com.generate3d.entity.ModelEvaluation;
import com.generate3d.entity.ModelUserRating;
import com.generate3d.service.ModelEvaluationService;
import com.generate3d.service.UserRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 评估控制器集成测试
 */
@WebMvcTest(EvaluationController.class)
@ActiveProfiles("test")
public class EvaluationControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @MockBean
    private ModelEvaluationService evaluationService;

    @MockBean
    private UserRatingService ratingService;

    private String testModelId;
    private String testTaskId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testModelId = "test_model_001";
        testTaskId = "test_task_001";
        testUserId = "test_user_001";
    }

    @Test
    void testCreateEvaluationTask() throws Exception {
        // 准备测试数据
        when(evaluationService.createEvaluationTask(testModelId, "FULL", "HIGH"))
                .thenReturn(testTaskId);

        // 执行测试
        mockMvc.perform(post("/v1/evaluation/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "modelId", testModelId,
                        "evaluationType", "FULL",
                        "priority", "HIGH"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(testTaskId));

        verify(evaluationService, times(1)).createEvaluationTask(testModelId, "FULL", "HIGH");
    }

    @Test
    void testGetTaskStatus() throws Exception {
        // 准备测试数据
        EvaluationTask task = createTestTask();
        when(evaluationService.getTaskStatus(testTaskId)).thenReturn(task);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/tasks/{taskId}", testTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(testTaskId))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.progress").value(0));

        verify(evaluationService, times(1)).getTaskStatus(testTaskId);
    }

    @Test
    void testGetEvaluationResult() throws Exception {
        // 准备测试数据
        ModelEvaluation evaluation = createTestEvaluation();
        when(evaluationService.getEvaluationResult(testModelId)).thenReturn(evaluation);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/results/{modelId}", testModelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.modelId").value(testModelId))
                .andExpect(jsonPath("$.data.overallScore").value(8.5))
                .andExpect(jsonPath("$.data.grade").value("GOOD"));

        verify(evaluationService, times(1)).getEvaluationResult(testModelId);
    }

    @Test
    void testGetEvaluationHistory() throws Exception {
        // 准备测试数据
        List<ModelEvaluation> history = Arrays.asList(
                createTestEvaluation(),
                createTestEvaluation()
        );
        when(evaluationService.getEvaluationHistory(testModelId, 10)).thenReturn(history);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/history/{modelId}", testModelId)
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(evaluationService, times(1)).getEvaluationHistory(testModelId, 10);
    }

    @Test
    void testReEvaluateModel() throws Exception {
        // 准备测试数据
        when(evaluationService.createEvaluationTask(testModelId, "FULL", "HIGH"))
                .thenReturn(testTaskId);

        // 执行测试
        mockMvc.perform(post("/v1/evaluation/re-evaluate/{modelId}", testModelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "evaluationType", "FULL",
                        "priority", "HIGH",
                        "reason", "用户请求重新评估"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(testTaskId));

        verify(evaluationService, times(1)).createEvaluationTask(testModelId, "FULL", "HIGH");
    }

    @Test
    void testSubmitUserRating() throws Exception {
        // 准备测试数据
        ModelUserRating rating = createTestRating();
        when(ratingService.submitRating(any(ModelUserRating.class))).thenReturn(rating);

        Map<String, Object> ratingRequest = new HashMap<>();
        ratingRequest.put("modelId", testModelId);
        ratingRequest.put("userId", testUserId);
        ratingRequest.put("overallRating", 4.5);
        ratingRequest.put("qualityRating", 4.0);
        ratingRequest.put("accuracyRating", 5.0);
        ratingRequest.put("visualRating", 4.5);
        ratingRequest.put("feedbackText", "很好的模型");
        ratingRequest.put("isAnonymous", false);

        // 执行测试
        mockMvc.perform(post("/v1/evaluation/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.modelId").value(testModelId))
                .andExpect(jsonPath("$.data.overallRating").value(4.5));

        verify(ratingService, times(1)).submitRating(any(ModelUserRating.class));
    }

    @Test
    void testGetUserRating() throws Exception {
        // 准备测试数据
        ModelUserRating rating = createTestRating();
        when(ratingService.getUserRating(testModelId, testUserId)).thenReturn(rating);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/ratings/{modelId}/user/{userId}", testModelId, testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.modelId").value(testModelId))
                .andExpect(jsonPath("$.data.userId").value(testUserId));

        verify(ratingService, times(1)).getUserRating(testModelId, testUserId);
    }

    @Test
    void testGetModelRatingStats() throws Exception {
        // 准备测试数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRatings", 100);
        stats.put("averageRating", 4.2);
        stats.put("ratingDistribution", Map.of("5", 30, "4", 40, "3", 20, "2", 8, "1", 2));
        
        when(ratingService.getModelRatingStats(testModelId)).thenReturn(stats);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/ratings/{modelId}/stats", testModelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRatings").value(100))
                .andExpect(jsonPath("$.data.averageRating").value(4.2));

        verify(ratingService, times(1)).getModelRatingStats(testModelId);
    }

    @Test
    void testGetSystemOverview() throws Exception {
        // 准备测试数据
        Map<String, Object> overview = new HashMap<>();
        overview.put("task_stats", Map.of("total", 100, "completed", 80, "running", 10));
        overview.put("evaluation_stats", Map.of("total", 85, "completed", 80));
        overview.put("rating_stats", Map.of("total", 200, "average_rating", 4.3));
        
        when(evaluationService.getSystemOverview()).thenReturn(overview);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/system/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.task_stats.total").value(100))
                .andExpect(jsonPath("$.data.evaluation_stats.total").value(85));

        verify(evaluationService, times(1)).getSystemOverview();
    }

    @Test
    void testCreateEvaluationTaskWithInvalidParams() throws Exception {
        // 测试无效参数
        mockMvc.perform(post("/v1/evaluation/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "modelId", "",
                        "evaluationType", "INVALID",
                        "priority", "UNKNOWN"
                ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNonExistentTask() throws Exception {
        // 准备测试数据
        when(evaluationService.getTaskStatus("non_existent_task")).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/tasks/{taskId}", "non_existent_task"))
                .andExpect(status().isNotFound());

        verify(evaluationService, times(1)).getTaskStatus("non_existent_task");
    }

    @Test
    void testSubmitRatingWithInvalidData() throws Exception {
        // 测试无效评分数据
        Map<String, Object> invalidRating = new HashMap<>();
        invalidRating.put("modelId", "");
        invalidRating.put("userId", "");
        invalidRating.put("overallRating", 6.0); // 超出范围
        
        mockMvc.perform(post("/v1/evaluation/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRating)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUserRating() throws Exception {
        // 准备测试数据
        when(ratingService.deleteRating(testModelId, testUserId)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/v1/evaluation/ratings/{modelId}/user/{userId}", testModelId, testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(ratingService, times(1)).deleteRating(testModelId, testUserId);
    }

    @Test
    void testGetRatingTrend() throws Exception {
        // 准备测试数据
        List<Map<String, Object>> trend = Arrays.asList(
                Map.of("date", "2024-01-01", "count", 10, "avgRating", 4.2),
                Map.of("date", "2024-01-02", "count", 15, "avgRating", 4.3)
        );
        when(ratingService.getRatingTrend(30)).thenReturn(trend);

        // 执行测试
        mockMvc.perform(get("/v1/evaluation/ratings/trend")
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(ratingService, times(1)).getRatingTrend(30);
    }

    // 辅助方法
    private EvaluationTask createTestTask() {
        EvaluationTask task = new EvaluationTask();
        task.setTaskId(testTaskId);
        task.setModelId(testModelId);
        task.setEvaluationType("FULL");
        task.setPriority("HIGH");
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setRetryCount(0);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    private ModelEvaluation createTestEvaluation() {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(testModelId);
        evaluation.setOverallScore(8.5);
        evaluation.setGeometricScore(8.0);
        evaluation.setVisualScore(9.0);
        evaluation.setTechnicalScore(8.5);
        evaluation.setGrade("GOOD");
        evaluation.setStatus("COMPLETED");
        evaluation.setEvaluatedAt(LocalDateTime.now());
        return evaluation;
    }

    private ModelUserRating createTestRating() {
        ModelUserRating rating = new ModelUserRating();
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
        return rating;
    }
}