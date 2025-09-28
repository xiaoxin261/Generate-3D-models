package com.generate3d.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.generate3d.entity.ModelUserRating;
import com.generate3d.mapper.ModelUserRatingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户评分服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRatingService {
    
    private final ModelUserRatingMapper ratingMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis缓存键前缀
    private static final String RATING_CACHE_PREFIX = "rating:";
    private static final String RATING_STATS_PREFIX = "rating_stats:";
    private static final int CACHE_EXPIRE_HOURS = 12;
    
    /**
     * 提交用户评分
     */
    public ModelUserRating submitRating(String modelId, Long userId, BigDecimal overallRating, 
                                       BigDecimal qualityRating, BigDecimal accuracyRating, 
                                       BigDecimal visualRating, String feedbackText, 
                                       List<String> feedbackTags, boolean isAnonymous) {
        log.info("提交用户评分 - 模型ID: {}, 用户ID: {}, 总体评分: {}", modelId, userId, overallRating);
        
        try {
            // 1. 验证评分数据
            validateRatingData(overallRating, qualityRating, accuracyRating, visualRating);
            
            // 2. 检查是否已有评分
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId).eq("user_id", userId);
            ModelUserRating existingRating = ratingMapper.selectOne(queryWrapper);
            
            ModelUserRating rating;
            if (existingRating != null) {
                // 更新现有评分
                rating = existingRating;
                rating.setOverallRating(overallRating);
                rating.setQualityRating(qualityRating);
                rating.setAccuracyRating(accuracyRating);
                rating.setVisualRating(visualRating);
                rating.setFeedbackText(feedbackText);
                rating.setFeedbackTags(feedbackTags != null ? String.join(",", feedbackTags) : null);
                rating.setIsAnonymous(isAnonymous ? 1 : 0);
                rating.setStatus(1);
                
                ratingMapper.updateById(rating);
                log.info("更新用户评分成功 - ID: {}", rating.getId());
            } else {
                // 创建新评分
                rating = new ModelUserRating();
                rating.setModelId(modelId);
                rating.setUserId(userId);
                rating.setOverallRating(overallRating);
                rating.setQualityRating(qualityRating);
                rating.setAccuracyRating(accuracyRating);
                rating.setVisualRating(visualRating);
                rating.setFeedbackText(feedbackText);
                rating.setFeedbackTags(feedbackTags != null ? String.join(",", feedbackTags) : null);
                rating.setIsAnonymous(isAnonymous ? 1 : 0);
                rating.setStatus(1);
                
                ratingMapper.insert(rating);
                log.info("创建用户评分成功 - ID: {}", rating.getId());
            }
            
            // 3. 清除相关缓存
            clearRatingCache(modelId);
            
            return rating;
            
        } catch (Exception e) {
            log.error("提交用户评分失败 - 模型ID: {}, 用户ID: {}", modelId, userId, e);
            throw new RuntimeException("提交评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型的用户评分统计
     */
    public Map<String, Object> getModelRatingStats(String modelId) {
        log.debug("获取模型评分统计 - 模型ID: {}", modelId);
        
        try {
            // 先从缓存获取
            Map<String, Object> cached = getCachedRatingStats(modelId);
            if (cached != null) {
                return cached;
            }
            
            // 从数据库计算统计数据
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId).eq("status", 1);
            List<ModelUserRating> ratings = ratingMapper.selectList(queryWrapper);
            
            Map<String, Object> stats = calculateRatingStats(ratings);
            
            // 缓存统计结果
            cacheRatingStats(modelId, stats);
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取模型评分统计失败 - 模型ID: {}", modelId, e);
            return getDefaultStats();
        }
    }
    
    /**
     * 获取用户对模型的评分
     */
    public ModelUserRating getUserRating(String modelId, Long userId) {
        try {
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId)
                       .eq("user_id", userId)
                       .eq("status", 1);
            
            return ratingMapper.selectOne(queryWrapper);
            
        } catch (Exception e) {
            log.error("获取用户评分失败 - 模型ID: {}, 用户ID: {}", modelId, userId, e);
            return null;
        }
    }
    
    /**
     * 获取模型的评分列表
     */
    public IPage<ModelUserRating> getModelRatings(String modelId, int page, int size, String sortBy) {
        try {
            Page<ModelUserRating> pageRequest = new Page<>(page, size);
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId).eq("status", 1);
            
            // 排序
            if ("rating_desc".equals(sortBy)) {
                queryWrapper.orderByDesc("overall_rating");
            } else if ("rating_asc".equals(sortBy)) {
                queryWrapper.orderByAsc("overall_rating");
            } else {
                queryWrapper.orderByDesc("created_at");
            }
            
            return ratingMapper.selectPage(pageRequest, queryWrapper);
            
        } catch (Exception e) {
            log.error("获取模型评分列表失败 - 模型ID: {}", modelId, e);
            return new Page<>();
        }
    }
    
    /**
     * 获取用户的评分历史
     */
    public IPage<ModelUserRating> getUserRatingHistory(Long userId, int page, int size) {
        try {
            Page<ModelUserRating> pageRequest = new Page<>(page, size);
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("status", 1)
                       .orderByDesc("created_at");
            
            return ratingMapper.selectPage(pageRequest, queryWrapper);
            
        } catch (Exception e) {
            log.error("获取用户评分历史失败 - 用户ID: {}", userId, e);
            return new Page<>();
        }
    }
    
    /**
     * 删除用户评分
     */
    public boolean deleteUserRating(String modelId, Long userId) {
        try {
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId).eq("user_id", userId);
            ModelUserRating rating = ratingMapper.selectOne(queryWrapper);
            
            if (rating != null) {
                // 软删除：设置状态为无效
                rating.setStatus(0);
                ratingMapper.updateById(rating);
                
                // 清除缓存
                clearRatingCache(modelId);
                
                log.info("删除用户评分成功 - 模型ID: {}, 用户ID: {}", modelId, userId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("删除用户评分失败 - 模型ID: {}, 用户ID: {}", modelId, userId, e);
            return false;
        }
    }
    
    /**
     * 获取热门评分标签
     */
    public List<Map<String, Object>> getPopularFeedbackTags(String modelId, int limit) {
        try {
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId)
                       .eq("status", 1)
                       .isNotNull("feedback_tags");
            
            List<ModelUserRating> ratings = ratingMapper.selectList(queryWrapper);
            
            // 统计标签频次
            Map<String, Integer> tagCounts = new HashMap<>();
            for (ModelUserRating rating : ratings) {
                if (StringUtils.hasText(rating.getFeedbackTags())) {
                    String[] tags = rating.getFeedbackTags().split(",");
                    for (String tag : tags) {
                        tag = tag.trim();
                        if (!tag.isEmpty()) {
                            tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                        }
                    }
                }
            }
            
            // 按频次排序并返回前N个
            return tagCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        Map<String, Object> tagInfo = new HashMap<>();
                        tagInfo.put("tag", entry.getKey());
                        tagInfo.put("count", entry.getValue());
                        return tagInfo;
                    })
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            
        } catch (Exception e) {
            log.error("获取热门评分标签失败 - 模型ID: {}", modelId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取评分趋势数据
     */
    public Map<String, Object> getRatingTrend(String modelId, int days) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            
            QueryWrapper<ModelUserRating> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId)
                       .eq("status", 1)
                       .ge("created_at", startDate)
                       .orderByAsc("created_at");
            
            List<ModelUserRating> ratings = ratingMapper.selectList(queryWrapper);
            
            Map<String, Object> trend = new HashMap<>();
            trend.put("total_ratings", ratings.size());
            trend.put("period_days", days);
            
            if (!ratings.isEmpty()) {
                // 按日期分组统计
                Map<String, List<ModelUserRating>> dailyRatings = new HashMap<>();
                for (ModelUserRating rating : ratings) {
                    String date = rating.getCreatedAt().toLocalDate().toString();
                    dailyRatings.computeIfAbsent(date, k -> new ArrayList<>()).add(rating);
                }
                
                List<Map<String, Object>> dailyStats = new ArrayList<>();
                for (Map.Entry<String, List<ModelUserRating>> entry : dailyRatings.entrySet()) {
                    Map<String, Object> dayStat = new HashMap<>();
                    dayStat.put("date", entry.getKey());
                    dayStat.put("count", entry.getValue().size());
                    
                    double avgRating = entry.getValue().stream()
                            .mapToDouble(r -> r.getOverallRating().doubleValue())
                            .average()
                            .orElse(0.0);
                    dayStat.put("avg_rating", BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
                    
                    dailyStats.add(dayStat);
                }
                
                trend.put("daily_stats", dailyStats);
            } else {
                trend.put("daily_stats", new ArrayList<>());
            }
            
            return trend;
            
        } catch (Exception e) {
            log.error("获取评分趋势失败 - 模型ID: {}", modelId, e);
            return new HashMap<>();
        }
    }
    
    // ========== 私有方法 ==========
    
    private void validateRatingData(BigDecimal overallRating, BigDecimal qualityRating, 
                                   BigDecimal accuracyRating, BigDecimal visualRating) {
        if (overallRating == null) {
            throw new IllegalArgumentException("总体评分不能为空");
        }
        
        if (overallRating.compareTo(BigDecimal.ONE) < 0 || overallRating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("总体评分必须在1-5之间");
        }
        
        // 验证其他评分（如果提供的话）
        validateRatingRange(qualityRating, "质量评分");
        validateRatingRange(accuracyRating, "准确性评分");
        validateRatingRange(visualRating, "视觉效果评分");
    }
    
    private void validateRatingRange(BigDecimal rating, String ratingName) {
        if (rating != null && (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new IllegalArgumentException(ratingName + "必须在1-5之间");
        }
    }
    
    private Map<String, Object> calculateRatingStats(List<ModelUserRating> ratings) {
        Map<String, Object> stats = new HashMap<>();
        
        if (ratings.isEmpty()) {
            return getDefaultStats();
        }
        
        // 基础统计
        stats.put("total_ratings", ratings.size());
        
        // 平均评分
        double avgOverall = ratings.stream()
                .mapToDouble(r -> r.getOverallRating().doubleValue())
                .average()
                .orElse(0.0);
        stats.put("avg_overall_rating", BigDecimal.valueOf(avgOverall).setScale(2, RoundingMode.HALF_UP));
        
        // 各维度平均评分
        OptionalDouble avgQuality = ratings.stream()
                .filter(r -> r.getQualityRating() != null)
                .mapToDouble(r -> r.getQualityRating().doubleValue())
                .average();
        if (avgQuality.isPresent()) {
            stats.put("avg_quality_rating", BigDecimal.valueOf(avgQuality.getAsDouble()).setScale(2, RoundingMode.HALF_UP));
        }
        
        OptionalDouble avgAccuracy = ratings.stream()
                .filter(r -> r.getAccuracyRating() != null)
                .mapToDouble(r -> r.getAccuracyRating().doubleValue())
                .average();
        if (avgAccuracy.isPresent()) {
            stats.put("avg_accuracy_rating", BigDecimal.valueOf(avgAccuracy.getAsDouble()).setScale(2, RoundingMode.HALF_UP));
        }
        
        OptionalDouble avgVisual = ratings.stream()
                .filter(r -> r.getVisualRating() != null)
                .mapToDouble(r -> r.getVisualRating().doubleValue())
                .average();
        if (avgVisual.isPresent()) {
            stats.put("avg_visual_rating", BigDecimal.valueOf(avgVisual.getAsDouble()).setScale(2, RoundingMode.HALF_UP));
        }
        
        // 评分分布
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("5_star", ratings.stream().filter(r -> r.getOverallRating().intValue() == 5).count());
        distribution.put("4_star", ratings.stream().filter(r -> r.getOverallRating().intValue() == 4).count());
        distribution.put("3_star", ratings.stream().filter(r -> r.getOverallRating().intValue() == 3).count());
        distribution.put("2_star", ratings.stream().filter(r -> r.getOverallRating().intValue() == 2).count());
        distribution.put("1_star", ratings.stream().filter(r -> r.getOverallRating().intValue() == 1).count());
        stats.put("rating_distribution", distribution);
        
        // 满意度统计
        long positiveRatings = ratings.stream().filter(r -> r.getOverallRating().doubleValue() >= 4.0).count();
        long negativeRatings = ratings.stream().filter(r -> r.getOverallRating().doubleValue() <= 2.0).count();
        
        stats.put("positive_ratings", positiveRatings);
        stats.put("negative_ratings", negativeRatings);
        stats.put("satisfaction_rate", BigDecimal.valueOf((double) positiveRatings / ratings.size() * 100).setScale(1, RoundingMode.HALF_UP));
        
        // 反馈统计
        long feedbackCount = ratings.stream().filter(r -> StringUtils.hasText(r.getFeedbackText())).count();
        stats.put("feedback_count", feedbackCount);
        stats.put("feedback_rate", BigDecimal.valueOf((double) feedbackCount / ratings.size() * 100).setScale(1, RoundingMode.HALF_UP));
        
        return stats;
    }
    
    private Map<String, Object> getDefaultStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_ratings", 0);
        stats.put("avg_overall_rating", BigDecimal.ZERO);
        stats.put("positive_ratings", 0);
        stats.put("negative_ratings", 0);
        stats.put("satisfaction_rate", BigDecimal.ZERO);
        stats.put("feedback_count", 0);
        stats.put("feedback_rate", BigDecimal.ZERO);
        
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("5_star", 0L);
        distribution.put("4_star", 0L);
        distribution.put("3_star", 0L);
        distribution.put("2_star", 0L);
        distribution.put("1_star", 0L);
        stats.put("rating_distribution", distribution);
        
        return stats;
    }
    
    private void clearRatingCache(String modelId) {
        try {
            String statsKey = RATING_STATS_PREFIX + modelId;
            redisTemplate.delete(statsKey);
        } catch (Exception e) {
            log.warn("清除评分缓存失败: {}", e.getMessage());
        }
    }
    
    private void cacheRatingStats(String modelId, Map<String, Object> stats) {
        try {
            String key = RATING_STATS_PREFIX + modelId;
            redisTemplate.opsForValue().set(key, stats, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存评分统计失败: {}", e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getCachedRatingStats(String modelId) {
        try {
            String key = RATING_STATS_PREFIX + modelId;
            return (Map<String, Object>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("获取缓存评分统计失败: {}", e.getMessage());
            return null;
        }
    }
}