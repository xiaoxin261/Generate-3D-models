package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.EvaluationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评估任务数据访问接口
 */
@Mapper
public interface EvaluationTaskMapper extends BaseMapper<EvaluationTask> {
    
    /**
     * 根据任务ID查询任务
     */
    @Select("SELECT * FROM evaluation_tasks WHERE task_id = #{taskId}")
    EvaluationTask selectByTaskId(@Param("taskId") String taskId);
    
    /**
     * 根据模型ID查询最新任务
     */
    @Select("SELECT * FROM evaluation_tasks WHERE model_id = #{modelId} ORDER BY created_at DESC LIMIT 1")
    EvaluationTask selectLatestByModelId(@Param("modelId") String modelId);
    
    /**
     * 根据模型ID查询任务历史
     */
    @Select("SELECT * FROM evaluation_tasks WHERE model_id = #{modelId} ORDER BY created_at DESC LIMIT #{limit}")
    List<EvaluationTask> selectHistoryByModelId(@Param("modelId") String modelId, @Param("limit") int limit);
    
    /**
     * 更新任务状态
     */
    @Update("UPDATE evaluation_tasks SET status = #{status}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateStatus(@Param("taskId") String taskId, @Param("status") String status);
    
    /**
     * 更新任务进度
     */
    @Update("UPDATE evaluation_tasks SET progress = #{progress}, current_step = #{currentStep}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateProgress(@Param("taskId") String taskId, @Param("progress") Integer progress, @Param("currentStep") String currentStep);
    
    /**
     * 更新任务状态和进度
     */
    @Update("UPDATE evaluation_tasks SET status = #{status}, progress = #{progress}, current_step = #{currentStep}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateStatusAndProgress(@Param("taskId") String taskId, @Param("status") String status, @Param("progress") Integer progress, @Param("currentStep") String currentStep);
    
    /**
     * 开始任务执行
     */
    @Update("UPDATE evaluation_tasks SET status = 'RUNNING', started_at = NOW(), executor_node_id = #{nodeId}, updated_at = NOW() WHERE task_id = #{taskId}")
    int startTask(@Param("taskId") String taskId, @Param("nodeId") String nodeId);
    
    /**
     * 完成任务
     */
    @Update("UPDATE evaluation_tasks SET status = 'COMPLETED', progress = 100, completed_at = NOW(), execution_time = #{executionTime}, updated_at = NOW() WHERE task_id = #{taskId}")
    int completeTask(@Param("taskId") String taskId, @Param("executionTime") Integer executionTime);
    
    /**
     * 任务失败
     */
    @Update("UPDATE evaluation_tasks SET status = 'FAILED', error_message = #{errorMessage}, completed_at = NOW(), updated_at = NOW() WHERE task_id = #{taskId}")
    int failTask(@Param("taskId") String taskId, @Param("errorMessage") String errorMessage);
    
    /**
     * 增加重试次数
     */
    @Update("UPDATE evaluation_tasks SET retry_count = retry_count + 1, updated_at = NOW() WHERE task_id = #{taskId}")
    int incrementRetryCount(@Param("taskId") String taskId);
    
    /**
     * 获取待执行的任务
     */
    @Select("SELECT * FROM evaluation_tasks WHERE status = 'PENDING' ORDER BY priority DESC, created_at ASC LIMIT #{limit}")
    List<EvaluationTask> selectPendingTasks(@Param("limit") int limit);
    
    /**
     * 获取运行中的任务
     */
    @Select("SELECT * FROM evaluation_tasks WHERE status = 'RUNNING' ORDER BY started_at ASC")
    List<EvaluationTask> selectRunningTasks();
    
    /**
     * 获取超时的任务
     */
    @Select("SELECT * FROM evaluation_tasks WHERE status = 'RUNNING' AND started_at < #{timeoutTime}")
    List<EvaluationTask> selectTimeoutTasks(@Param("timeoutTime") LocalDateTime timeoutTime);
    
    /**
     * 获取可重试的失败任务
     */
    @Select("SELECT * FROM evaluation_tasks WHERE status = 'FAILED' AND retry_count < #{maxRetries} ORDER BY updated_at ASC LIMIT #{limit}")
    List<EvaluationTask> selectRetryableTasks(@Param("maxRetries") int maxRetries, @Param("limit") int limit);
    
    /**
     * 根据状态统计任务数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_tasks WHERE status = #{status}")
    Long countByStatus(@Param("status") String status);
    
    /**
     * 根据优先级统计任务数量
     */
    @Select("SELECT priority, COUNT(*) as count FROM evaluation_tasks GROUP BY priority")
    List<Map<String, Object>> countByPriority();
    
    /**
     * 根据评估类型统计任务数量
     */
    @Select("SELECT evaluation_type, COUNT(*) as count FROM evaluation_tasks GROUP BY evaluation_type")
    List<Map<String, Object>> countByEvaluationType();
    
    /**
     * 获取任务执行统计
     */
    @Select("SELECT " +
            "AVG(execution_time) as avg_execution_time, " +
            "MIN(execution_time) as min_execution_time, " +
            "MAX(execution_time) as max_execution_time " +
            "FROM evaluation_tasks WHERE status = 'COMPLETED' AND execution_time IS NOT NULL")
    Map<String, Object> getExecutionStats();
    
    /**
     * 获取任务趋势数据
     */
    @Select("SELECT " +
            "DATE(created_at) as date, " +
            "COUNT(*) as total_tasks, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks, " +
            "SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_tasks " +
            "FROM evaluation_tasks " +
            "WHERE created_at >= #{startDate} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getTaskTrend(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 获取节点执行统计
     */
    @Select("SELECT " +
            "executor_node_id, " +
            "COUNT(*) as task_count, " +
            "AVG(execution_time) as avg_execution_time, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failure_count " +
            "FROM evaluation_tasks " +
            "WHERE executor_node_id IS NOT NULL " +
            "GROUP BY executor_node_id " +
            "ORDER BY task_count DESC")
    List<Map<String, Object>> getNodeStats();
    
    /**
     * 清理过期任务
     */
    @Update("DELETE FROM evaluation_tasks WHERE status IN ('COMPLETED', 'FAILED') AND updated_at < #{beforeDate}")
    int cleanupExpiredTasks(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * 重置超时任务
     */
    @Update("UPDATE evaluation_tasks SET status = 'PENDING', executor_node_id = NULL, error_message = 'Task timeout, reset to pending', updated_at = NOW() WHERE status = 'RUNNING' AND started_at < #{timeoutTime}")
    int resetTimeoutTasks(@Param("timeoutTime") LocalDateTime timeoutTime);
    
    /**
     * 获取最近的任务
     */
    @Select("SELECT * FROM evaluation_tasks ORDER BY created_at DESC LIMIT #{limit}")
    List<EvaluationTask> selectRecent(@Param("limit") int limit);
    
    /**
     * 根据时间范围统计任务数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_tasks WHERE created_at >= #{startDate} AND created_at <= #{endDate}")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取任务成功率
     */
    @Select("SELECT " +
            "COUNT(*) as total_tasks, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks, " +
            "SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_tasks, " +
            "ROUND(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM evaluation_tasks")
    Map<String, Object> getSuccessRate();
    
    /**
     * 统计总任务数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_tasks")
    Long countTotal();
}