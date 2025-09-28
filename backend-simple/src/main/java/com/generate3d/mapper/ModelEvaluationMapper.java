package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.ModelEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模型评估数据访问接口
 */
@Mapper
public interface ModelEvaluationMapper extends BaseMapper<ModelEvaluation> {
    
    /**
     * 根据模型ID查询最新评估结果
     */
    @Select("SELECT * FROM model_evaluations WHERE model_id = #{modelId} ORDER BY created_at DESC LIMIT 1")
    ModelEvaluation selectLatestByModelId(@Param("modelId") String modelId);
    
    /**
     * 根据模型ID查询评估历史
     */
    @Select("SELECT * FROM model_evaluations WHERE model_id = #{modelId} ORDER BY created_at DESC LIMIT #{limit}")
    List<ModelEvaluation> selectHistoryByModelId(@Param("modelId") String modelId, @Param("limit") int limit);
    
    /**
     * 更新评估状态
     */
    @Update("UPDATE model_evaluations SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 更新评估结果
     */
    @Update("UPDATE model_evaluations SET " +
            "geometric_score = #{geometricScore}, " +
            "visual_score = #{visualScore}, " +
            "technical_score = #{technicalScore}, " +
            "overall_score = #{overallScore}, " +
            "grade = #{grade}, " +
            "status = #{status}, " +
            "evaluation_time = #{evaluationTime}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateEvaluationResult(@Param("id") Long id,
                              @Param("geometricScore") BigDecimal geometricScore,
                              @Param("visualScore") BigDecimal visualScore,
                              @Param("technicalScore") BigDecimal technicalScore,
                              @Param("overallScore") BigDecimal overallScore,
                              @Param("grade") String grade,
                              @Param("status") String status,
                              @Param("evaluationTime") Integer evaluationTime);
    
    /**
     * 更新错误信息
     */
    @Update("UPDATE model_evaluations SET error_message = #{errorMessage}, status = 'FAILED', updated_at = NOW() WHERE id = #{id}")
    int updateErrorMessage(@Param("id") Long id, @Param("errorMessage") String errorMessage);
    
    /**
     * 统计各等级模型数量
     */
    @Select("SELECT grade, COUNT(*) as count FROM model_evaluations WHERE status = 'COMPLETED' GROUP BY grade")
    List<Map<String, Object>> countByGrade();
    
    /**
     * 统计评估状态分布
     */
    @Select("SELECT status, COUNT(*) as count FROM model_evaluations GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 获取平均评分统计
     */
    @Select("SELECT " +
            "AVG(geometric_score) as avg_geometric_score, " +
            "AVG(visual_score) as avg_visual_score, " +
            "AVG(technical_score) as avg_technical_score, " +
            "AVG(overall_score) as avg_overall_score " +
            "FROM model_evaluations WHERE status = 'COMPLETED'")
    Map<String, Object> getAverageScores();
    
    /**
     * 获取评估趋势数据
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count, AVG(overall_score) as avg_score " +
            "FROM model_evaluations " +
            "WHERE status = 'COMPLETED' AND created_at >= #{startDate} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getEvaluationTrend(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 获取高质量模型列表
     */
    @Select("SELECT model_id, overall_score, grade FROM model_evaluations " +
            "WHERE status = 'COMPLETED' AND overall_score >= #{minScore} " +
            "ORDER BY overall_score DESC LIMIT #{limit}")
    List<Map<String, Object>> getHighQualityModels(@Param("minScore") BigDecimal minScore, @Param("limit") int limit);
    
    /**
     * 获取需要重新评估的模型
     */
    @Select("SELECT * FROM model_evaluations " +
            "WHERE status = 'FAILED' OR (status = 'COMPLETED' AND updated_at < #{beforeDate}) " +
            "ORDER BY updated_at ASC LIMIT #{limit}")
    List<ModelEvaluation> getModelsNeedReEvaluation(@Param("beforeDate") LocalDateTime beforeDate, @Param("limit") int limit);
    
    /**
     * 根据评分范围查询模型
     */
    @Select("SELECT * FROM model_evaluations " +
            "WHERE status = 'COMPLETED' " +
            "AND overall_score >= #{minScore} AND overall_score <= #{maxScore} " +
            "ORDER BY overall_score DESC")
    List<ModelEvaluation> selectByScoreRange(@Param("minScore") BigDecimal minScore, @Param("maxScore") BigDecimal maxScore);
    
    /**
     * 根据等级查询模型
     */
    @Select("SELECT * FROM model_evaluations WHERE grade = #{grade} AND status = 'COMPLETED' ORDER BY overall_score DESC")
    List<ModelEvaluation> selectByGrade(@Param("grade") String grade);
    
    /**
     * 统计总评估数量
     */
    @Select("SELECT COUNT(*) FROM model_evaluations")
    Long countTotal();
    
    /**
     * 统计已完成评估数量
     */
    @Select("SELECT COUNT(*) FROM model_evaluations WHERE status = 'COMPLETED'")
    Long countCompleted();
    
    /**
     * 统计失败评估数量
     */
    @Select("SELECT COUNT(*) FROM model_evaluations WHERE status = 'FAILED'")
    Long countFailed();
    
    /**
     * 统计进行中评估数量
     */
    @Select("SELECT COUNT(*) FROM model_evaluations WHERE status = 'IN_PROGRESS'")
    Long countInProgress();
    
    /**
     * 获取最近的评估记录
     */
    @Select("SELECT * FROM model_evaluations ORDER BY created_at DESC LIMIT #{limit}")
    List<ModelEvaluation> selectRecent(@Param("limit") int limit);
    
    /**
     * 根据时间范围统计评估数量
     */
    @Select("SELECT COUNT(*) FROM model_evaluations WHERE created_at >= #{startDate} AND created_at <= #{endDate}")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}