package com.generate3d.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generate3d.entity.GenerationTask;
import com.generate3d.mapper.GenerationTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 生成任务服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationTaskService extends ServiceImpl<GenerationTaskMapper, GenerationTask> {
    
    private final GenerationTaskMapper taskMapper;
    
    /**
     * 根据任务ID获取任务
     */
    public GenerationTask getByTaskId(String taskId) {
        return taskMapper.selectByTaskId(taskId);
    }
    
    /**
     * 更新任务状态
     */
    public boolean updateStatusByTaskId(String taskId, String status, Integer progress) {
        return taskMapper.updateStatus(taskId, status, progress) > 0;
    }
    
    /**
     * 更新任务开始时间
     */
    public boolean updateStartTime(String taskId, LocalDateTime startTime) {
        return taskMapper.updateStartTime(taskId, startTime) > 0;
    }
    
    /**
     * 完成任务
     */
    public boolean completeTask(String taskId, String modelId, LocalDateTime completedAt, Integer actualTime) {
        return taskMapper.completeTask(taskId, modelId, completedAt, actualTime) > 0;
    }
    
    /**
     * 任务失败
     */
    public boolean failTask(String taskId, String errorMessage) {
        return taskMapper.failTask(taskId, errorMessage) > 0;
    }
    
    /**
     * 获取正在运行的任务
     */
    public List<GenerationTask> getRunningTasks() {
        return taskMapper.selectRunningTasks();
    }
    
    /**
     * 获取超时任务
     */
    public List<GenerationTask> getTimeoutTasks(int timeoutMinutes) {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return taskMapper.selectTimeoutTasks(timeoutTime);
    }
    
    /**
     * 获取任务统计
     */
    public Map<String, Long> getTaskStatistics() {
        return Map.of(
            "pending", taskMapper.countByStatus("pending"),
            "processing", taskMapper.countByStatus("processing"),
            "completed", taskMapper.countByStatus("completed"),
            "failed", taskMapper.countByStatus("failed")
        );
    }
    
    /**
     * 清理过期任务
     */
    public int cleanupExpiredTasks(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return taskMapper.delete(
            lambdaQuery()
                .lt(GenerationTask::getCreatedAt, cutoffDate)
                .in(GenerationTask::getStatus, "completed", "failed")
        );
    }
    
    /**
     * 重试失败任务
     */
    public boolean retryFailedTask(String taskId) {
        GenerationTask task = getByTaskId(taskId);
        if (task == null || !"failed".equals(task.getStatus())) {
            return false;
        }
        
        // 重置任务状态
        task.setStatus("pending");
        task.setProgress(0);
        task.setErrorMessage(null);
        task.setStartedAt(null);
        task.setCompletedAt(null);
        task.setActualTime(null);
        
        return updateById(task);
    }
    
    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        GenerationTask task = getByTaskId(taskId);
        if (task == null) {
            return false;
        }
        
        // 只能取消待处理或处理中的任务
        if (!"pending".equals(task.getStatus()) && !"processing".equals(task.getStatus())) {
            return false;
        }
        
        task.setStatus("cancelled");
        task.setCompletedAt(LocalDateTime.now());
        
        return updateById(task);
    }
}