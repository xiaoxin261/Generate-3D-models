package com.generate3d.service;

import com.generate3d.entity.EvaluationTask;
import com.generate3d.entity.ModelEvaluation;
import com.generate3d.mapper.EvaluationTaskMapper;
import com.generate3d.mapper.ModelEvaluationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 模型评估服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ModelEvaluationServiceTest {

    @Resource
    private ModelEvaluationService evaluationService;

    @MockBean
    private EvaluationTaskMapper taskMapper;

    @MockBean
    private ModelEvaluationMapper evaluationMapper;

    private String testModelId;
    private String testTaskId;

    @BeforeEach
    void setUp() {
        testModelId = "test_model_001";
        testTaskId = "test_task_001";
    }

    @Test
    void testCreateEvaluationTask() {
        // 准备测试数据
        when(taskMapper.insert(any(EvaluationTask.class))).thenReturn(1);

        // 执行测试
        String taskId = evaluationService.createEvaluationTask(testModelId, "FULL", "HIGH");

        // 验证结果
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("eval_"));
        verify(taskMapper, times(1)).insert(any(EvaluationTask.class));
    }

    @Test
    void testExecuteEvaluationAsync() {
        // 准备测试数据
        EvaluationTask task = createTestTask();
        when(taskMapper.selectByTaskId(testTaskId)).thenReturn(task);
        when(taskMapper.updateById(any(EvaluationTask.class))).thenReturn(1);
        when(evaluationMapper.insert(any(ModelEvaluation.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> {
            evaluationService.executeEvaluationAsync(testTaskId);
        });

        // 验证任务状态更新
        verify(taskMapper, atLeastOnce()).updateById(any(EvaluationTask.class));
    }

    @Test
    void testGetEvaluationResult() {
        // 准备测试数据
        ModelEvaluation evaluation = createTestEvaluation();
        when(evaluationMapper.selectLatestByModelId(testModelId)).thenReturn(evaluation);

        // 执行测试
        ModelEvaluation result = evaluationService.getEvaluationResult(testModelId);

        // 验证结果
        assertNotNull(result);
        assertEquals(testModelId, result.getModelId());
        assertEquals(8.5, result.getOverallScore());
        assertEquals("GOOD", result.getGrade());
    }

    @Test
    void testGetTaskStatus() {
        // 准备测试数据
        EvaluationTask task = createTestTask();
        task.setStatus("COMPLETED");
        task.setProgress(100);
        when(taskMapper.selectByTaskId(testTaskId)).thenReturn(task);

        // 执行测试
        EvaluationTask result = evaluationService.getTaskStatus(testTaskId);

        // 验证结果
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(100, result.getProgress());
    }

    @Test
    void testGetSystemOverview() {
        // 准备测试数据
        when(taskMapper.countTotal()).thenReturn(100L);
        when(taskMapper.countByStatus("COMPLETED")).thenReturn(80L);
        when(taskMapper.countByStatus("RUNNING")).thenReturn(10L);
        when(taskMapper.countByStatus("FAILED")).thenReturn(5L);
        when(evaluationMapper.countTotal()).thenReturn(85L);

        // 执行测试
        Map<String, Object> overview = evaluationService.getSystemOverview();

        // 验证结果
        assertNotNull(overview);
        assertTrue(overview.containsKey("task_stats"));
        assertTrue(overview.containsKey("evaluation_stats"));

        @SuppressWarnings("unchecked")
        Map<String, Object> taskStats = (Map<String, Object>) overview.get("task_stats");
        assertEquals(100L, taskStats.get("total"));
        assertEquals(80L, taskStats.get("completed"));
    }

    @Test
    void testEvaluationWithInvalidModel() {
        // 测试无效模型ID的情况
        when(taskMapper.selectByTaskId(anyString())).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            evaluationService.executeEvaluationAsync("invalid_task_id");
        });
    }

    @Test
    void testEvaluationTaskRetry() {
        // 准备测试数据 - 失败的任务
        EvaluationTask failedTask = createTestTask();
        failedTask.setStatus("FAILED");
        failedTask.setRetryCount(1);
        failedTask.setErrorMessage("Previous evaluation failed");

        when(taskMapper.selectByTaskId(testTaskId)).thenReturn(failedTask);
        when(taskMapper.updateById(any(EvaluationTask.class))).thenReturn(1);

        // 执行重试
        assertDoesNotThrow(() -> {
            evaluationService.executeEvaluationAsync(testTaskId);
        });

        // 验证重试次数增加
        verify(taskMapper, atLeastOnce()).updateById(argThat(task -> 
            task.getRetryCount() > 1
        ));
    }

    @Test
    void testConcurrentEvaluations() throws InterruptedException {
        // 准备多个测试任务
        EvaluationTask task1 = createTestTask();
        task1.setTaskId("task_001");
        EvaluationTask task2 = createTestTask();
        task2.setTaskId("task_002");

        when(taskMapper.selectByTaskId("task_001")).thenReturn(task1);
        when(taskMapper.selectByTaskId("task_002")).thenReturn(task2);
        when(taskMapper.updateById(any(EvaluationTask.class))).thenReturn(1);
        when(evaluationMapper.insert(any(ModelEvaluation.class))).thenReturn(1);

        // 并发执行评估
        Thread thread1 = new Thread(() -> evaluationService.executeEvaluationAsync("task_001"));
        Thread thread2 = new Thread(() -> evaluationService.executeEvaluationAsync("task_002"));

        thread1.start();
        thread2.start();

        thread1.join(5000); // 最多等待5秒
        thread2.join(5000);

        // 验证两个任务都被处理
        verify(taskMapper, times(2)).selectByTaskId(anyString());
    }

    @Test
    void testEvaluationResultCaching() {
        // 准备测试数据
        ModelEvaluation evaluation = createTestEvaluation();
        when(evaluationMapper.selectLatestByModelId(testModelId)).thenReturn(evaluation);

        // 第一次调用
        ModelEvaluation result1 = evaluationService.getEvaluationResult(testModelId);
        
        // 第二次调用（应该使用缓存）
        ModelEvaluation result2 = evaluationService.getEvaluationResult(testModelId);

        // 验证结果一致
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getOverallScore(), result2.getOverallScore());
        
        // 验证数据库只被调用一次（由于缓存）
        verify(evaluationMapper, times(1)).selectLatestByModelId(testModelId);
    }

    // 辅助方法
    private EvaluationTask createTestTask() {
        EvaluationTask task = new EvaluationTask();
        task.setTaskId(testTaskId);
        task.setModelId(testModelId);
        task.setEvaluationType("FULL");
        task.setPriority(5); // 使用Integer类型
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
}