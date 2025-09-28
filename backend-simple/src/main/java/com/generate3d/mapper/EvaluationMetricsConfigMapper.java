package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.EvaluationMetricsConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 评估指标配置数据访问接口
 */
@Mapper
public interface EvaluationMetricsConfigMapper extends BaseMapper<EvaluationMetricsConfig> {
    
    /**
     * 获取所有启用的指标配置
     */
    @Select("SELECT * FROM evaluation_metrics_config WHERE is_enabled = 1 ORDER BY metric_category, metric_name")
    List<EvaluationMetricsConfig> selectEnabledMetrics();
    
    /**
     * 根据指标分类获取配置
     */
    @Select("SELECT * FROM evaluation_metrics_config WHERE metric_category = #{category} AND is_enabled = 1 ORDER BY metric_name")
    List<EvaluationMetricsConfig> selectByCategory(String category);
    
    /**
     * 根据指标名称获取配置
     */
    @Select("SELECT * FROM evaluation_metrics_config WHERE metric_name = #{metricName}")
    EvaluationMetricsConfig selectByMetricName(String metricName);
    
    /**
     * 更新指标权重
     */
    @Update("UPDATE evaluation_metrics_config SET weight = #{weight}, updated_at = NOW() WHERE id = #{id}")
    int updateWeight(Long id, Double weight);
    
    /**
     * 更新指标阈值
     */
    @Update("UPDATE evaluation_metrics_config SET threshold_excellent = #{excellent}, threshold_good = #{good}, threshold_fair = #{fair}, updated_at = NOW() WHERE id = #{id}")
    int updateThresholds(Long id, Double excellent, Double good, Double fair);
    
    /**
     * 启用/禁用指标
     */
    @Update("UPDATE evaluation_metrics_config SET is_enabled = #{enabled}, updated_at = NOW() WHERE id = #{id}")
    int updateEnabled(Long id, Boolean enabled);
    
    /**
     * 获取指标分类统计
     */
    @Select("SELECT metric_category, COUNT(*) as count, SUM(CASE WHEN is_enabled = 1 THEN 1 ELSE 0 END) as enabled_count FROM evaluation_metrics_config GROUP BY metric_category")
    List<Map<String, Object>> getCategoryStats();
    
    /**
     * 获取权重总和（用于验证权重配置）
     */
    @Select("SELECT metric_category, SUM(weight) as total_weight FROM evaluation_metrics_config WHERE is_enabled = 1 GROUP BY metric_category")
    List<Map<String, Object>> getWeightSumByCategory();
    
    /**
     * 获取所有指标分类
     */
    @Select("SELECT DISTINCT metric_category FROM evaluation_metrics_config ORDER BY metric_category")
    List<String> selectAllCategories();
    
    /**
     * 批量更新指标状态
     */
    @Update("UPDATE evaluation_metrics_config SET is_enabled = #{enabled}, updated_at = NOW() WHERE metric_category = #{category}")
    int updateEnabledByCategory(String category, Boolean enabled);
    
    /**
     * 重置指标配置为默认值
     */
    @Update("UPDATE evaluation_metrics_config SET weight = #{defaultWeight}, threshold_excellent = #{defaultExcellent}, threshold_good = #{defaultGood}, threshold_fair = #{defaultFair}, updated_at = NOW() WHERE id = #{id}")
    int resetToDefault(Long id, Double defaultWeight, Double defaultExcellent, Double defaultGood, Double defaultFair);
    
    /**
     * 获取指标配置历史（如果有历史表的话）
     */
    @Select("SELECT * FROM evaluation_metrics_config WHERE metric_name = #{metricName} ORDER BY updated_at DESC LIMIT #{limit}")
    List<EvaluationMetricsConfig> selectConfigHistory(String metricName, Integer limit);
    
    /**
     * 统计总指标数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_metrics_config")
    Long countTotal();
    
    /**
     * 统计启用的指标数量
     */
    @Select("SELECT COUNT(*) FROM evaluation_metrics_config WHERE is_enabled = 1")
    Long countEnabled();
    
    /**
     * 获取最近更新的指标配置
     */
    @Select("SELECT * FROM evaluation_metrics_config ORDER BY updated_at DESC LIMIT #{limit}")
    List<EvaluationMetricsConfig> selectRecentUpdated(Integer limit);
}