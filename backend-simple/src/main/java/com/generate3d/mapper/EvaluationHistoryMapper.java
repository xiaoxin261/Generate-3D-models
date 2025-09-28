package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.EvaluationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评估历史数据访问接口
 */
@Mapper
public interface EvaluationHistoryMapper extends BaseMapper<EvaluationHistory> {
    
    /**
     * 根据模型ID获取评估历史
     */
    @Select("SELECT * FROM evaluation_history WHERE model_id = #{modelId} ORDER BY created_at DESC")
    List<EvaluationHistory> selectByModelId(String modelId);
    
    /**
     * 根据评估ID获取历史记录
     */
    @Select("SELECT * FROM evaluation_history WHERE evaluation_id = #{evaluationId} ORDER BY version DESC")
    List<EvaluationHistory> selectByEvaluationId(String evaluationId);
    
    /**
     * 获取模型的最新评估历史
     */
    @Select("SELECT * FROM evaluation_history WHERE model_id = #{modelId} ORDER BY version DESC LIMIT 1")
    EvaluationHistory selectLatestByModelId(String modelId);
    
    /**
     * 根据版本号获取历史记录
     */
    @Select("SELECT * FROM evaluation_history WHERE model_id = #{modelId} AND version = #{version}")
    EvaluationHistory selectByModelIdAndVersion(String modelId, Integer version);
    
    /**
     * 获取指定时间范围内的评估历史
     */
    @Select("SELECT * FROM evaluation_history WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<EvaluationHistory> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据变更原因获取历史记录
     */
    @Select("SELECT * FROM evaluation_history WHERE change_reason = #{changeReason} ORDER BY created_at DESC LIMIT #{limit}")
    List<EvaluationHistory> selectByChangeReason(String changeReason, Integer limit);
    
    /**
     * 根据变更人获取历史记录
     */
    @Select("SELECT * FROM evaluation_history WHERE changed_by = #{changedBy} ORDER BY created_at DESC LIMIT #{limit}")
    List<EvaluationHistory> selectByChangedBy(String changedBy, Integer limit);
    
    /**
     * 获取评估历史统计
     */
    @Select("SELECT change_reason, COUNT(*) as count FROM evaluation_history GROUP BY change_reason ORDER BY count DESC")
    List<Map<String, Object>> getChangeReasonStats();
    
    /**
     * 获取用户变更统计
     */
    @Select("SELECT changed_by, COUNT(*) as count FROM evaluation_history WHERE changed_by IS NOT NULL GROUP BY changed_by ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserChangeStats(Integer limit);
    
    /**
     * 获取评估历史趋势
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM evaluation_history WHERE created_at >= #{startDate} GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> getHistoryTrend(LocalDateTime startDate);
    
    /**
     * 获取模型评估版本数量
     */
    @Select("SELECT model_id, COUNT(*) as version_count FROM evaluation_history GROUP BY model_id ORDER BY version_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getModelVersionStats(Integer limit);
    
    /**
     * 获取最近的评估历史记录
     */
    @Select("SELECT * FROM evaluation_history ORDER BY created_at DESC LIMIT #{limit}")
    List<EvaluationHistory> selectRecent(Integer limit);
    
    /**
     * 删除过期的历史记录
     */
    @Select("DELETE FROM evaluation_history WHERE created_at < #{expireTime}")
    int deleteExpiredHistory(LocalDateTime expireTime);
    
    /**
     * 统计总历史记录数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_history")
    Long countTotal();
    
    /**
     * 统计指定模型的历史记录数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_history WHERE model_id = #{modelId}")
    Long countByModelId(String modelId);
    
    /**
     * 统计自动更新的历史记录数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_history WHERE change_reason IN ('AUTO_EVALUATION', 'SCHEDULED_UPDATE', 'SYSTEM_UPDATE')")
    Long countAutoUpdates();
    
    /**
     * 统计手动更新的历史记录数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_history WHERE changed_by IS NOT NULL AND change_reason NOT IN ('AUTO_EVALUATION', 'SCHEDULED_UPDATE', 'SYSTEM_UPDATE')")
    Long countManualUpdates();
    
    /**
     * 获取评估分数变化趋势
     */
    @Select("SELECT model_id, version, JSON_EXTRACT(score_snapshot, '$.overall_score') as score, created_at FROM evaluation_history WHERE model_id = #{modelId} ORDER BY version")
    List<Map<String, Object>> getScoreTrend(String modelId);
    
    /**
     * 获取配置变更历史
     */
    @Select("SELECT * FROM evaluation_history WHERE config_snapshot IS NOT NULL AND config_snapshot != '' ORDER BY created_at DESC LIMIT #{limit}")
    List<EvaluationHistory> selectConfigChanges(Integer limit);
    
    /**
     * 根据分数范围获取历史记录
     */
    @Select("SELECT * FROM evaluation_history WHERE JSON_EXTRACT(score_snapshot, '$.overall_score') BETWEEN #{minScore} AND #{maxScore} ORDER BY created_at DESC")
    List<EvaluationHistory> selectByScoreRange(Double minScore, Double maxScore);
}