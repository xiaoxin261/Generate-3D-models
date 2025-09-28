package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.ModelUserRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户评分数据访问接口
 */
@Mapper
public interface ModelUserRatingMapper extends BaseMapper<ModelUserRating> {
    
    /**
     * 根据模型ID和用户ID查询评分
     */
    @Select("SELECT * FROM model_user_ratings WHERE model_id = #{modelId} AND user_id = #{userId} AND status = 1")
    ModelUserRating selectByModelIdAndUserId(@Param("modelId") String modelId, @Param("userId") Long userId);
    
    /**
     * 根据模型ID查询所有有效评分
     */
    @Select("SELECT * FROM model_user_ratings WHERE model_id = #{modelId} AND status = 1 ORDER BY created_at DESC")
    List<ModelUserRating> selectByModelId(@Param("modelId") String modelId);
    
    /**
     * 根据用户ID查询评分历史
     */
    @Select("SELECT * FROM model_user_ratings WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<ModelUserRating> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    /**
     * 更新评分状态
     */
    @Update("UPDATE model_user_ratings SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 软删除评分
     */
    @Update("UPDATE model_user_ratings SET status = 0, updated_at = NOW() WHERE model_id = #{modelId} AND user_id = #{userId}")
    int deleteByModelIdAndUserId(@Param("modelId") String modelId, @Param("userId") Long userId);
    
    /**
     * 统计模型的评分数量
     */
    @Select("SELECT COUNT(*) FROM model_user_ratings WHERE model_id = #{modelId} AND status = 1")
    Long countByModelId(@Param("modelId") String modelId);
    
    /**
     * 计算模型的平均评分
     */
    @Select("SELECT " +
            "AVG(overall_rating) as avg_overall_rating, " +
            "AVG(quality_rating) as avg_quality_rating, " +
            "AVG(accuracy_rating) as avg_accuracy_rating, " +
            "AVG(visual_rating) as avg_visual_rating " +
            "FROM model_user_ratings WHERE model_id = #{modelId} AND status = 1")
    Map<String, Object> getAverageRatingsByModelId(@Param("modelId") String modelId);
    
    /**
     * 获取模型评分分布
     */
    @Select("SELECT " +
            "FLOOR(overall_rating) as rating, " +
            "COUNT(*) as count " +
            "FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 " +
            "GROUP BY FLOOR(overall_rating) " +
            "ORDER BY rating DESC")
    List<Map<String, Object>> getRatingDistributionByModelId(@Param("modelId") String modelId);
    
    /**
     * 获取用户评分统计
     */
    @Select("SELECT COUNT(*) as total_ratings, AVG(overall_rating) as avg_rating " +
            "FROM model_user_ratings WHERE user_id = #{userId} AND status = 1")
    Map<String, Object> getUserRatingStats(@Param("userId") Long userId);
    
    /**
     * 获取热门评分标签
     */
    @Select("SELECT feedback_tags FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 AND feedback_tags IS NOT NULL AND feedback_tags != ''")
    List<String> getFeedbackTagsByModelId(@Param("modelId") String modelId);
    
    /**
     * 获取最近的评分记录
     */
    @Select("SELECT * FROM model_user_ratings WHERE status = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<ModelUserRating> selectRecent(@Param("limit") int limit);
    
    /**
     * 根据评分范围查询
     */
    @Select("SELECT * FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 " +
            "AND overall_rating >= #{minRating} AND overall_rating <= #{maxRating} " +
            "ORDER BY created_at DESC")
    List<ModelUserRating> selectByRatingRange(@Param("modelId") String modelId, 
                                             @Param("minRating") BigDecimal minRating, 
                                             @Param("maxRating") BigDecimal maxRating);
    
    /**
     * 获取高分评价
     */
    @Select("SELECT * FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 AND overall_rating >= #{minRating} " +
            "ORDER BY overall_rating DESC, created_at DESC")
    List<ModelUserRating> selectHighRatings(@Param("modelId") String modelId, @Param("minRating") BigDecimal minRating);
    
    /**
     * 获取低分评价
     */
    @Select("SELECT * FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 AND overall_rating <= #{maxRating} " +
            "ORDER BY overall_rating ASC, created_at DESC")
    List<ModelUserRating> selectLowRatings(@Param("modelId") String modelId, @Param("maxRating") BigDecimal maxRating);
    
    /**
     * 获取有反馈文本的评分
     */
    @Select("SELECT * FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 " +
            "AND feedback_text IS NOT NULL AND feedback_text != '' " +
            "ORDER BY created_at DESC")
    List<ModelUserRating> selectWithFeedback(@Param("modelId") String modelId);
    
    /**
     * 统计时间范围内的评分数量
     */
    @Select("SELECT COUNT(*) FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 " +
            "AND created_at >= #{startDate} AND created_at <= #{endDate}")
    Long countByDateRange(@Param("modelId") String modelId, 
                         @Param("startDate") LocalDateTime startDate, 
                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取评分趋势数据
     */
    @Select("SELECT " +
            "DATE(created_at) as date, " +
            "COUNT(*) as count, " +
            "AVG(overall_rating) as avg_rating " +
            "FROM model_user_ratings " +
            "WHERE model_id = #{modelId} AND status = 1 " +
            "AND created_at >= #{startDate} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getRatingTrend(@Param("modelId") String modelId, @Param("startDate") LocalDateTime startDate);
    
    /**
     * 获取用户活跃度统计
     */
    @Select("SELECT " +
            "user_id, " +
            "COUNT(*) as rating_count, " +
            "AVG(overall_rating) as avg_rating, " +
            "MAX(created_at) as last_rating_time " +
            "FROM model_user_ratings " +
            "WHERE status = 1 " +
            "GROUP BY user_id " +
            "ORDER BY rating_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getActiveUsers(@Param("limit") int limit);
    
    /**
     * 统计总评分数量
     */
    @Select("SELECT COUNT(*) FROM model_user_ratings WHERE status = 1")
    Long countTotal();
    
    /**
     * 统计匿名评分数量
     */
    @Select("SELECT COUNT(*) FROM model_user_ratings WHERE status = 1 AND is_anonymous = 1")
    Long countAnonymous();
    
    /**
     * 统计有反馈的评分数量
     */
    @Select("SELECT COUNT(*) FROM model_user_ratings WHERE status = 1 AND feedback_text IS NOT NULL AND feedback_text != ''")
    Long countWithFeedback();
}