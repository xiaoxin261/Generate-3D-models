package com.generate3d.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generate3d.entity.ExportTask;
import com.generate3d.mapper.ExportTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 导出任务服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportTaskService extends ServiceImpl<ExportTaskMapper, ExportTask> {
    
    private final ExportTaskMapper exportTaskMapper;
    
    /**
     * 根据导出ID获取任务
     */
    public ExportTask getByExportId(String exportId) {
        return exportTaskMapper.selectByExportId(exportId);
    }
    
    /**
     * 更新任务状态
     */
    public boolean updateStatusByExportId(String exportId, String status, Integer progress) {
        return exportTaskMapper.updateStatus(exportId, status, progress) > 0;
    }
    
    /**
     * 完成任务
     */
    public boolean completeTask(String exportId, String outputPath, Long fileSize, String downloadUrl) {
        return exportTaskMapper.completeTask(exportId, outputPath, fileSize, downloadUrl, LocalDateTime.now()) > 0;
    }
    
    /**
     * 任务失败
     */
    public boolean failTask(String exportId, String errorMessage) {
        return exportTaskMapper.failTask(exportId, errorMessage) > 0;
    }
    
    /**
     * 获取正在运行的任务
     */
    public List<ExportTask> getRunningTasks() {
        return exportTaskMapper.selectRunningTasks();
    }
    
    /**
     * 获取过期任务
     */
    public List<ExportTask> getExpiredTasks() {
        return exportTaskMapper.selectExpiredTasks(LocalDateTime.now());
    }
    
    /**
     * 删除过期任务
     */
    public int deleteExpiredTasks() {
        return exportTaskMapper.deleteExpiredTasks(LocalDateTime.now());
    }
    
    /**
     * 获取任务统计
     */
    public Map<String, Long> getTaskStatistics() {
        return Map.of(
            "pending", exportTaskMapper.countByStatus("pending"),
            "processing", exportTaskMapper.countByStatus("processing"),
            "completed", exportTaskMapper.countByStatus("completed"),
            "failed", exportTaskMapper.countByStatus("failed")
        );
    }
    
    /**
     * 清理过期任务
     */
    public int cleanupExpiredTasks(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return exportTaskMapper.delete(
            lambdaQuery()
                .lt(ExportTask::getCreatedAt, cutoffDate)
                .in(ExportTask::getStatus, "completed", "failed")
        );
    }
    
    /**
     * 取消任务
     */
    public boolean cancelTask(String exportId) {
        ExportTask task = getByExportId(exportId);
        if (task == null) {
            return false;
        }
        
        // 只能取消待处理或处理中的任务
        if (!"pending".equals(task.getStatus()) && !"processing".equals(task.getStatus())) {
            return false;
        }
        
        task.setStatus("cancelled");
        return updateById(task);
    }
    
    /**
     * 重试失败任务
     */
    public boolean retryFailedTask(String exportId) {
        ExportTask task = getByExportId(exportId);
        if (task == null || !"failed".equals(task.getStatus())) {
            return false;
        }
        
        // 重置任务状态
        task.setStatus("pending");
        task.setProgress(0);
        task.setOutputPath(null);
        task.setFileSize(null);
        task.setDownloadUrl(null);
        
        return updateById(task);
    }
}