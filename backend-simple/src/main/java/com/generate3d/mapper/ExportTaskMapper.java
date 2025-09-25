package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.ExportTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导出任务数据访问接口
 */
@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTask> {
    
    /**
     * 根据导出ID查询任务
     */
    @Select("SELECT * FROM t_export_task WHERE export_id = #{exportId}")
    ExportTask selectByExportId(@Param("exportId") String exportId);
    
    /**
     * 更新任务状态
     */
    @Update("UPDATE t_export_task SET status = #{status}, progress = #{progress}, updated_at = NOW() WHERE export_id = #{exportId}")
    int updateStatusByExportId(@Param("exportId") String exportId, @Param("status") String status, @Param("progress") Integer progress);
    
    /**
     * 更新任务状态（别名）
     */
    @Update("UPDATE t_export_task SET status = #{status}, progress = #{progress}, updated_at = NOW() WHERE export_id = #{exportId}")
    int updateStatus(@Param("exportId") String exportId, @Param("status") String status, @Param("progress") Integer progress);
    
    /**
     * 完成导出任务
     */
    @Update("UPDATE t_export_task SET status = 'completed', progress = 100, output_path = #{outputPath}, file_size = #{fileSize}, download_url = #{downloadUrl}, updated_at = NOW() WHERE export_id = #{exportId}")
    int completeExportTask(@Param("exportId") String exportId, @Param("outputPath") String outputPath, @Param("fileSize") Long fileSize, @Param("downloadUrl") String downloadUrl);
    
    /**
     * 完成导出任务（带时间戳）
     */
    @Update("UPDATE t_export_task SET status = 'completed', progress = 100, output_path = #{outputPath}, file_size = #{fileSize}, download_url = #{downloadUrl}, completed_at = #{completedAt}, updated_at = NOW() WHERE export_id = #{exportId}")
    int completeTask(@Param("exportId") String exportId, @Param("outputPath") String outputPath, @Param("fileSize") Long fileSize, @Param("downloadUrl") String downloadUrl, @Param("completedAt") java.time.LocalDateTime completedAt);
    
    /**
     * 导出任务失败
     */
    @Update("UPDATE t_export_task SET status = 'failed', updated_at = NOW() WHERE export_id = #{exportId}")
    int failExportTask(@Param("exportId") String exportId);
    
    /**
     * 导出任务失败（带错误消息）
     */
    @Update("UPDATE t_export_task SET status = 'failed', error_message = #{errorMessage}, updated_at = NOW() WHERE export_id = #{exportId}")
    int failTask(@Param("exportId") String exportId, @Param("errorMessage") String errorMessage);
    
    /**
     * 查询进行中的导出任务
     */
    @Select("SELECT * FROM t_export_task WHERE status IN ('pending', 'processing') ORDER BY created_at ASC")
    List<ExportTask> selectRunningTasks();
    
    /**
     * 查询过期的导出任务
     */
    @Select("SELECT * FROM t_export_task WHERE expires_at < #{currentTime} AND status = 'completed'")
    List<ExportTask> selectExpiredTasks(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 删除过期的导出文件记录
     */
    @Update("DELETE FROM t_export_task WHERE expires_at < #{currentTime} AND status = 'completed'")
    int deleteExpiredTasks(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 统计导出任务数量
     */
    @Select("SELECT COUNT(*) FROM t_export_task WHERE status = #{status}")
    Long countByStatus(@Param("status") String status);
}