package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.GenerationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成任务数据访问接口
 */
@Mapper
public interface GenerationTaskMapper extends BaseMapper<GenerationTask> {
    
    /**
     * 根据任务ID查询任务
     */
    @Select("SELECT * FROM t_generation_task WHERE task_id = #{taskId}")
    GenerationTask selectByTaskId(@Param("taskId") String taskId);
    
    /**
     * 更新任务状态
     */
    @Update("UPDATE t_generation_task SET status = #{status}, progress = #{progress}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateStatusByTaskId(@Param("taskId") String taskId, @Param("status") String status, @Param("progress") Integer progress);
    
    /**
     * 更新任务开始时间
     */
    @Update("UPDATE t_generation_task SET started_at = #{startedAt}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateStartedAtByTaskId(@Param("taskId") String taskId, @Param("startedAt") LocalDateTime startedAt);
    
    /**
     * 更新任务开始时间（别名）
     */
    @Update("UPDATE t_generation_task SET started_at = #{startedAt}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateStartTime(@Param("taskId") String taskId, @Param("startedAt") LocalDateTime startedAt);
    
    /**
     * 更新任务状态（别名）
     */
    @Update("UPDATE t_generation_task SET status = #{status}, progress = #{progress}, updated_at = NOW() WHERE task_id = #{taskId}")
    int updateStatus(@Param("taskId") String taskId, @Param("status") String status, @Param("progress") Integer progress);
    
    /**
     * 完成任务
     */
    @Update("UPDATE t_generation_task SET status = 'completed', progress = 100, model_id = #{modelId}, completed_at = #{completedAt}, actual_time = #{actualTime}, updated_at = NOW() WHERE task_id = #{taskId}")
    int completeTask(@Param("taskId") String taskId, @Param("modelId") String modelId, @Param("completedAt") LocalDateTime completedAt, @Param("actualTime") Integer actualTime);
    
    /**
     * 任务失败
     */
    @Update("UPDATE t_generation_task SET status = 'failed', error_message = #{errorMessage}, completed_at = NOW(), updated_at = NOW() WHERE task_id = #{taskId}")
    int failTask(@Param("taskId") String taskId, @Param("errorMessage") String errorMessage);
    
    /**
     * 查询进行中的任务
     */
    @Select("SELECT * FROM t_generation_task WHERE status IN ('pending', 'processing') ORDER BY created_at ASC")
    List<GenerationTask> selectRunningTasks();
    
    /**
     * 查询超时的任务
     */
    @Select("SELECT * FROM t_generation_task WHERE status = 'processing' AND started_at < #{timeoutTime}")
    List<GenerationTask> selectTimeoutTasks(@Param("timeoutTime") LocalDateTime timeoutTime);
    
    /**
     * 统计任务数量
     */
    @Select("SELECT COUNT(*) FROM t_generation_task WHERE status = #{status}")
    Long countByStatus(@Param("status") String status);
}