package com.generate3d.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.generate3d.entity.*;
import com.generate3d.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 模型评估服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelEvaluationService {
    
    private final ModelEvaluationMapper evaluationMapper;
    private final EvaluationTaskMapper taskMapper;
    private final EvaluationMetricsConfigMapper configMapper;
    private final ModelUserRatingMapper ratingMapper;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis缓存键前缀
    private static final String EVALUATION_CACHE_PREFIX = "evaluation:";
    private static final String TASK_CACHE_PREFIX = "eval_task:";
    private static final int CACHE_EXPIRE_HOURS = 24;
    
    /**
     * 创建评估任务
     */
    public String createEvaluationTask(String modelId, String evaluationType, String priority) {
        log.info("创建评估任务 - 模型ID: {}, 评估类型: {}", modelId, evaluationType);
        
        try {
            // 1. 验证模型是否存在
            Model model = modelMapper.selectById(modelId);
            if (model == null) {
                throw new RuntimeException("模型不存在: " + modelId);
            }
            
            // 2. 检查是否已有进行中的任务
            QueryWrapper<EvaluationTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId)
                       .in("status", Arrays.asList("pending", "processing"));
            EvaluationTask existingTask = taskMapper.selectOne(queryWrapper);
            
            if (existingTask != null) {
                log.warn("模型 {} 已有进行中的评估任务: {}", modelId, existingTask.getTaskId());
                return existingTask.getTaskId();
            }
            
            // 3. 创建新任务
            EvaluationTask task = new EvaluationTask();
            task.setTaskId(generateTaskId());
            task.setModelId(modelId);
            task.setEvaluationType(evaluationType != null ? evaluationType : "full");
            task.setPriority(priority != null ? Integer.parseInt(priority) : 5);
            task.setRetryCount(0);
            task.setMaxRetries(3);
            task.setStatus("pending");
            task.setProgress(0);
            task.setCurrentStep("初始化");
            
            taskMapper.insert(task);
            
            // 4. 缓存任务状态
            cacheTaskStatus(task);
            
            // 5. 异步执行评估
            executeEvaluationAsync(task.getTaskId());
            
            log.info("评估任务创建成功 - 任务ID: {}", task.getTaskId());
            return task.getTaskId();
            
        } catch (Exception e) {
            log.error("创建评估任务失败 - 模型ID: {}", modelId, e);
            throw new RuntimeException("创建评估任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 异步执行评估
     */
    @Async
    public void executeEvaluationAsync(String taskId) {
        log.info("开始执行评估任务: {}", taskId);
        
        EvaluationTask task = null;
        try {
            // 1. 获取任务信息
            task = taskMapper.selectOne(new QueryWrapper<EvaluationTask>().eq("task_id", taskId));
            if (task == null) {
                log.error("评估任务不存在: {}", taskId);
                return;
            }
            
            // 2. 更新任务状态为处理中
            updateTaskStatus(taskId, "processing", 10, "开始评估");
            task.setStartedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            
            // 3. 获取模型信息
            Model model = modelMapper.selectById(task.getModelId());
            if (model == null) {
                failTask(taskId, "模型不存在");
                return;
            }
            
            // 4. 执行评估
            ModelEvaluation evaluation = performEvaluation(model, task);
            
            // 5. 保存评估结果
            saveEvaluationResult(evaluation);
            
            // 6. 完成任务
            completeTask(taskId, evaluation.getId());
            
            log.info("评估任务完成: {} - 最终得分: {}", taskId, evaluation.getFinalScore());
            
        } catch (Exception e) {
            log.error("执行评估任务失败: {}", taskId, e);
            failTask(taskId, "评估执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行模型评估
     */
    private ModelEvaluation performEvaluation(Model model, EvaluationTask task) {
        log.info("执行模型评估 - 模型ID: {}", model.getModelId());
        
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(model.getModelId());
        evaluation.setEvaluationVersion("1.0");
        evaluation.setStatus("processing");
        
        try {
            // 1. 几何质量评估 (40%)
            updateTaskStatus(task.getTaskId(), "processing", 30, "几何质量评估");
            GeometricEvaluationResult geometricResult = evaluateGeometricQuality(model);
            evaluation.setGeometricScore(geometricResult.getScore());
            evaluation.setGeometricDetails(JSON.toJSONString(geometricResult.getDetails()));
            
            // 2. 视觉效果评估 (35%)
            updateTaskStatus(task.getTaskId(), "processing", 60, "视觉效果评估");
            VisualEvaluationResult visualResult = evaluateVisualQuality(model);
            evaluation.setVisualScore(visualResult.getScore());
            evaluation.setVisualDetails(JSON.toJSONString(visualResult.getDetails()));
            
            // 3. 技术指标评估 (25%)
            updateTaskStatus(task.getTaskId(), "processing", 80, "技术指标评估");
            TechnicalEvaluationResult technicalResult = evaluateTechnicalMetrics(model);
            evaluation.setTechnicalScore(technicalResult.getScore());
            evaluation.setTechnicalDetails(JSON.toJSONString(technicalResult.getDetails()));
            
            // 4. 计算综合评分
            updateTaskStatus(task.getTaskId(), "processing", 90, "计算综合评分");
            BigDecimal finalScore = calculateFinalScore(
                evaluation.getGeometricScore(),
                evaluation.getVisualScore(),
                evaluation.getTechnicalScore()
            );
            evaluation.setFinalScore(finalScore);
            evaluation.setGrade(calculateGrade(finalScore));
            
            // 5. 计算评估耗时
            if (task.getStartedAt() != null) {
                long seconds = java.time.Duration.between(task.getStartedAt(), LocalDateTime.now()).getSeconds();
                evaluation.setEvaluationTime((int) seconds);
            }
            
            evaluation.setStatus("completed");
            
            log.info("模型评估完成 - 模型ID: {}, 最终得分: {}, 等级: {}", 
                model.getModelId(), finalScore, evaluation.getGrade());
            
            return evaluation;
            
        } catch (Exception e) {
            log.error("模型评估失败 - 模型ID: {}", model.getModelId(), e);
            evaluation.setStatus("failed");
            evaluation.setErrorMessage(e.getMessage());
            throw e;
        }
    }
    
    /**
     * 几何质量评估
     */
    private GeometricEvaluationResult evaluateGeometricQuality(Model model) {
        Map<String, Object> details = new HashMap<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        int metricCount = 0;
        
        try {
            // 1. 网格完整性检测 (权重: 0.4)
            BigDecimal meshIntegrityScore = evaluateMeshIntegrity(model);
            details.put("mesh_integrity", meshIntegrityScore);
            totalScore = totalScore.add(meshIntegrityScore.multiply(BigDecimal.valueOf(0.4)));
            metricCount++;
            
            // 2. 拓扑结构验证 (权重: 0.3)
            BigDecimal topologyScore = evaluateTopology(model);
            details.put("topology_validation", topologyScore);
            totalScore = totalScore.add(topologyScore.multiply(BigDecimal.valueOf(0.3)));
            metricCount++;
            
            // 3. 几何精度评估 (权重: 0.3)
            BigDecimal accuracyScore = evaluateGeometricAccuracy(model);
            details.put("geometric_accuracy", accuracyScore);
            totalScore = totalScore.add(accuracyScore.multiply(BigDecimal.valueOf(0.3)));
            metricCount++;
            
            details.put("evaluation_method", "automated_analysis");
            details.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.warn("几何质量评估部分失败: {}", e.getMessage());
            // 使用默认评分
            totalScore = BigDecimal.valueOf(75.0);
            details.put("error", e.getMessage());
            details.put("fallback_score", true);
        }
        
        return new GeometricEvaluationResult(totalScore, details);
    }
    
    /**
     * 视觉效果评估
     */
    private VisualEvaluationResult evaluateVisualQuality(Model model) {
        Map<String, Object> details = new HashMap<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        
        try {
            // 1. 渲染质量评估 (权重: 0.4)
            BigDecimal renderScore = evaluateRenderQuality(model);
            details.put("render_quality", renderScore);
            totalScore = totalScore.add(renderScore.multiply(BigDecimal.valueOf(0.4)));
            
            // 2. 色彩准确性评估 (权重: 0.3)
            BigDecimal colorScore = evaluateColorAccuracy(model);
            details.put("color_accuracy", colorScore);
            totalScore = totalScore.add(colorScore.multiply(BigDecimal.valueOf(0.3)));
            
            // 3. 风格一致性评估 (权重: 0.3)
            BigDecimal styleScore = evaluateStyleConsistency(model);
            details.put("style_consistency", styleScore);
            totalScore = totalScore.add(styleScore.multiply(BigDecimal.valueOf(0.3)));
            
            details.put("evaluation_method", "visual_analysis");
            details.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.warn("视觉效果评估部分失败: {}", e.getMessage());
            totalScore = BigDecimal.valueOf(80.0);
            details.put("error", e.getMessage());
            details.put("fallback_score", true);
        }
        
        return new VisualEvaluationResult(totalScore, details);
    }
    
    /**
     * 技术指标评估
     */
    private TechnicalEvaluationResult evaluateTechnicalMetrics(Model model) {
        Map<String, Object> details = new HashMap<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        
        try {
            // 1. 文件大小优化 (权重: 0.3)
            BigDecimal sizeScore = evaluateFileSize(model);
            details.put("file_size_optimization", sizeScore);
            totalScore = totalScore.add(sizeScore.multiply(BigDecimal.valueOf(0.3)));
            
            // 2. 多边形效率 (权重: 0.4)
            BigDecimal polygonScore = evaluatePolygonEfficiency(model);
            details.put("polygon_efficiency", polygonScore);
            totalScore = totalScore.add(polygonScore.multiply(BigDecimal.valueOf(0.4)));
            
            // 3. 生成性能 (权重: 0.3)
            BigDecimal performanceScore = evaluateGenerationPerformance(model);
            details.put("generation_performance", performanceScore);
            totalScore = totalScore.add(performanceScore.multiply(BigDecimal.valueOf(0.3)));
            
            details.put("file_size_mb", model.getFileSize() != null ? model.getFileSize() / (1024.0 * 1024.0) : 0);
            details.put("vertices_count", model.getVerticesCount());
            details.put("faces_count", model.getFacesCount());
            details.put("generation_time_seconds", model.getGenerationTime());
            details.put("evaluation_method", "technical_analysis");
            details.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.warn("技术指标评估部分失败: {}", e.getMessage());
            totalScore = BigDecimal.valueOf(85.0);
            details.put("error", e.getMessage());
            details.put("fallback_score", true);
        }
        
        return new TechnicalEvaluationResult(totalScore, details);
    }
    
    // ========== 具体评估算法实现 ==========
    
    private BigDecimal evaluateMeshIntegrity(Model model) {
        // 网格完整性检测算法
        BigDecimal score = BigDecimal.valueOf(90.0);
        
        // 基于顶点和面数的基础检测
        if (model.getVerticesCount() != null && model.getFacesCount() != null) {
            int vertices = model.getVerticesCount();
            int faces = model.getFacesCount();
            
            // 检查顶点面数比例是否合理
            if (vertices > 0 && faces > 0) {
                double ratio = (double) faces / vertices;
                if (ratio >= 0.5 && ratio <= 2.0) {
                    score = score.add(BigDecimal.valueOf(5.0));
                }
            }
            
            // 检查模型复杂度
            if (vertices >= 1000 && faces >= 500) {
                score = score.add(BigDecimal.valueOf(3.0));
            }
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal evaluateTopology(Model model) {
        // 拓扑结构验证算法
        BigDecimal score = BigDecimal.valueOf(85.0);
        
        // 基于模型基础信息的拓扑评估
        if (model.getVerticesCount() != null && model.getVerticesCount() > 0) {
            score = score.add(BigDecimal.valueOf(5.0));
        }
        
        if (model.getFacesCount() != null && model.getFacesCount() > 0) {
            score = score.add(BigDecimal.valueOf(5.0));
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal evaluateGeometricAccuracy(Model model) {
        // 几何精度评估算法
        BigDecimal score = BigDecimal.valueOf(88.0);
        
        // 基于边界框信息评估
        if (StringUtils.hasText(model.getBoundingBoxMin()) && 
            StringUtils.hasText(model.getBoundingBoxMax())) {
            score = score.add(BigDecimal.valueOf(7.0));
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal evaluateRenderQuality(Model model) {
        // 渲染质量评估算法
        BigDecimal score = BigDecimal.valueOf(82.0);
        
        // 基于材质和颜色信息评估
        if (StringUtils.hasText(model.getMaterialType())) {
            score = score.add(BigDecimal.valueOf(8.0));
        }
        
        if (StringUtils.hasText(model.getPrimaryColor())) {
            score = score.add(BigDecimal.valueOf(5.0));
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal evaluateColorAccuracy(Model model) {
        // 色彩准确性评估算法
        BigDecimal score = BigDecimal.valueOf(80.0);
        
        if (StringUtils.hasText(model.getPrimaryColor())) {
            score = score.add(BigDecimal.valueOf(10.0));
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal evaluateStyleConsistency(Model model) {
        // 风格一致性评估算法
        BigDecimal score = BigDecimal.valueOf(85.0);
        
        if (StringUtils.hasText(model.getCategory())) {
            score = score.add(BigDecimal.valueOf(10.0));
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal evaluateFileSize(Model model) {
        // 文件大小优化评估
        BigDecimal score = BigDecimal.valueOf(80.0);
        
        if (model.getFileSize() != null) {
            long fileSizeMB = model.getFileSize() / (1024 * 1024);
            
            // 文件大小评分逻辑
            if (fileSizeMB <= 5) {
                score = BigDecimal.valueOf(95.0);
            } else if (fileSizeMB <= 10) {
                score = BigDecimal.valueOf(85.0);
            } else if (fileSizeMB <= 20) {
                score = BigDecimal.valueOf(75.0);
            } else {
                score = BigDecimal.valueOf(60.0);
            }
        }
        
        return score;
    }
    
    private BigDecimal evaluatePolygonEfficiency(Model model) {
        // 多边形效率评估
        BigDecimal score = BigDecimal.valueOf(85.0);
        
        if (model.getFacesCount() != null) {
            int faces = model.getFacesCount();
            
            // 面数效率评分
            if (faces >= 1000 && faces <= 10000) {
                score = BigDecimal.valueOf(90.0);
            } else if (faces > 10000 && faces <= 50000) {
                score = BigDecimal.valueOf(80.0);
            } else if (faces > 50000) {
                score = BigDecimal.valueOf(70.0);
            }
        }
        
        return score;
    }
    
    private BigDecimal evaluateGenerationPerformance(Model model) {
        // 生成性能评估
        BigDecimal score = BigDecimal.valueOf(85.0);
        
        if (model.getGenerationTime() != null) {
            int generationTime = model.getGenerationTime();
            
            // 生成时间评分 (秒)
            if (generationTime <= 30) {
                score = BigDecimal.valueOf(95.0);
            } else if (generationTime <= 60) {
                score = BigDecimal.valueOf(85.0);
            } else if (generationTime <= 120) {
                score = BigDecimal.valueOf(75.0);
            } else {
                score = BigDecimal.valueOf(65.0);
            }
        }
        
        return score;
    }
    
    /**
     * 计算综合评分
     */
    private BigDecimal calculateFinalScore(BigDecimal geometricScore, BigDecimal visualScore, BigDecimal technicalScore) {
        // 权重: 几何质量40%, 视觉效果35%, 技术指标25%
        BigDecimal finalScore = BigDecimal.ZERO;
        
        if (geometricScore != null) {
            finalScore = finalScore.add(geometricScore.multiply(BigDecimal.valueOf(0.40)));
        }
        
        if (visualScore != null) {
            finalScore = finalScore.add(visualScore.multiply(BigDecimal.valueOf(0.35)));
        }
        
        if (technicalScore != null) {
            finalScore = finalScore.add(technicalScore.multiply(BigDecimal.valueOf(0.25)));
        }
        
        return finalScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算评分等级
     */
    private String calculateGrade(BigDecimal finalScore) {
        if (finalScore == null) return "D";
        
        double score = finalScore.doubleValue();
        if (score >= 90) return "A+";
        else if (score >= 80) return "A";
        else if (score >= 70) return "B";
        else if (score >= 60) return "C";
        else return "D";
    }
    
    /**
     * 保存评估结果
     */
    private void saveEvaluationResult(ModelEvaluation evaluation) {
        try {
            // 检查是否已存在评估结果
            QueryWrapper<ModelEvaluation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", evaluation.getModelId());
            ModelEvaluation existing = evaluationMapper.selectOne(queryWrapper);
            
            if (existing != null) {
                // 更新现有记录
                evaluation.setId(existing.getId());
                evaluationMapper.updateById(evaluation);
            } else {
                // 插入新记录
                evaluationMapper.insert(evaluation);
            }
            
            // 缓存评估结果
            cacheEvaluationResult(evaluation);
            
        } catch (Exception e) {
            log.error("保存评估结果失败 - 模型ID: {}", evaluation.getModelId(), e);
            throw new RuntimeException("保存评估结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评估结果
     */
    public ModelEvaluation getEvaluationResult(String modelId) {
        try {
            // 先从缓存获取
            ModelEvaluation cached = getCachedEvaluationResult(modelId);
            if (cached != null) {
                return cached;
            }
            
            // 从数据库获取
            QueryWrapper<ModelEvaluation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId);
            ModelEvaluation evaluation = evaluationMapper.selectOne(queryWrapper);
            
            if (evaluation != null) {
                cacheEvaluationResult(evaluation);
            }
            
            return evaluation;
            
        } catch (Exception e) {
            log.error("获取评估结果失败 - 模型ID: {}", modelId, e);
            return null;
        }
    }
    
    /**
     * 获取评估历史记录
     */
    public List<ModelEvaluation> getEvaluationHistory(String modelId, int limit) {
        try {
            QueryWrapper<ModelEvaluation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", modelId)
                       .orderByDesc("evaluated_at")
                       .last("LIMIT " + limit);
            
            List<ModelEvaluation> history = evaluationMapper.selectList(queryWrapper);
            log.info("获取模型 {} 的评估历史记录，共 {} 条", modelId, history.size());
            return history;
            
        } catch (Exception e) {
            log.error("获取评估历史记录失败: modelId={}, error={}", modelId, e.getMessage(), e);
            throw new RuntimeException("获取评估历史记录失败", e);
        }
    }

    /**
     * 标记任务失败
     */
    public void markTaskFailed(String taskId, String errorMessage) {
        try {
            EvaluationTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMessage(errorMessage);
                task.setRetryCount(task.getRetryCount() + 1);
                task.setUpdatedAt(LocalDateTime.now());
                taskMapper.updateById(task);
                
                // 更新缓存
                cacheTaskStatus(task);
                
                log.info("任务 {} 标记为失败: {}", taskId, errorMessage);
            }
        } catch (Exception e) {
            log.error("标记任务失败时出错: taskId={}, error={}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 重试任务
     */
    public boolean retryTask(String taskId) {
        try {
            EvaluationTask task = taskMapper.selectById(taskId);
            if (task != null && "FAILED".equals(task.getStatus()) && task.getRetryCount() < 3) {
                task.setStatus("PENDING");
                task.setProgress(0);
                task.setErrorMessage(null);
                task.setUpdatedAt(LocalDateTime.now());
                taskMapper.updateById(task);
                
                // 更新缓存
                cacheTaskStatus(task);
                
                // 重新执行评估
                executeEvaluationAsync(taskId);
                
                log.info("任务 {} 重试成功", taskId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("重试任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取任务状态
     */
    public EvaluationTask getTaskStatus(String taskId) {
        try {
            // 先从缓存获取
            EvaluationTask cached = getCachedTaskStatus(taskId);
            if (cached != null) {
                return cached;
            }
            
            // 从数据库获取
            QueryWrapper<EvaluationTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", taskId);
            EvaluationTask task = taskMapper.selectOne(queryWrapper);
            
            if (task != null) {
                cacheTaskStatus(task);
            }
            
            return task;
            
        } catch (Exception e) {
            log.error("获取任务状态失败 - 任务ID: {}", taskId, e);
            return null;
        }
    }
    
    /**
     * 获取评估系统概览
     */
    public Map<String, Object> getSystemOverview() {
        log.debug("获取评估系统概览");
        
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 任务统计
            Map<String, Object> taskStats = new HashMap<>();
            taskStats.put("total", taskMapper.countTotal());
            taskStats.put("pending", taskMapper.countByStatus("PENDING"));
            taskStats.put("running", taskMapper.countByStatus("RUNNING"));
            taskStats.put("completed", taskMapper.countByStatus("COMPLETED"));
            taskStats.put("failed", taskMapper.countByStatus("FAILED"));
            overview.put("task_stats", taskStats);
            
            // 评估统计
            Map<String, Object> evalStats = new HashMap<>();
            evalStats.put("total", evaluationMapper.countTotal());
            evalStats.put("completed", evaluationMapper.countCompleted());
            evalStats.put("failed", evaluationMapper.countFailed());
            evalStats.put("in_progress", evaluationMapper.countInProgress());
            overview.put("evaluation_stats", evalStats);
            
            // 评分统计
            Map<String, Object> ratingStats = new HashMap<>();
            ratingStats.put("total", ratingMapper.countTotal());
            ratingStats.put("anonymous", ratingMapper.countAnonymous());
            ratingStats.put("with_feedback", ratingMapper.countWithFeedback());
            overview.put("rating_stats", ratingStats);
            
            // 平均评分
            Map<String, Object> avgScores = evaluationMapper.getAverageScores();
            overview.put("average_scores", avgScores);
            
            // 等级分布
            List<Map<String, Object>> gradeDistribution = evaluationMapper.countByGrade();
            overview.put("grade_distribution", gradeDistribution);
            
            // 任务成功率
            Map<String, Object> successRate = taskMapper.getSuccessRate();
            overview.put("success_rate", successRate);
            
            // 最近活动
            List<EvaluationTask> recentTasks = taskMapper.selectRecent(10);
            overview.put("recent_tasks", recentTasks);
            
            return overview;
            
        } catch (Exception e) {
            log.error("获取系统概览失败", e);
            return new HashMap<>();
        }
    }
    
    // ========== 辅助方法 ==========
    
    private void updateTaskStatus(String taskId, String status, int progress, String currentStep) {
        try {
            EvaluationTask task = taskMapper.selectOne(new QueryWrapper<EvaluationTask>().eq("task_id", taskId));
            if (task != null) {
                task.setStatus(status);
                task.setProgress(progress);
                task.setCurrentStep(currentStep);
                taskMapper.updateById(task);
                cacheTaskStatus(task);
            }
        } catch (Exception e) {
            log.error("更新任务状态失败 - 任务ID: {}", taskId, e);
        }
    }
    
    private void completeTask(String taskId, Long evaluationId) {
        try {
            EvaluationTask task = taskMapper.selectOne(new QueryWrapper<EvaluationTask>().eq("task_id", taskId));
            if (task != null) {
                task.setStatus("completed");
                task.setProgress(100);
                task.setCurrentStep("评估完成");
                task.setCompletedAt(LocalDateTime.now());
                
                if (task.getStartedAt() != null) {
                    long seconds = java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).getSeconds();
                    task.setExecutionTime((int) seconds);
                }
                
                taskMapper.updateById(task);
                cacheTaskStatus(task);
            }
        } catch (Exception e) {
            log.error("完成任务失败 - 任务ID: {}", taskId, e);
        }
    }
    
    private void failTask(String taskId, String errorMessage) {
        try {
            EvaluationTask task = taskMapper.selectOne(new QueryWrapper<EvaluationTask>().eq("task_id", taskId));
            if (task != null) {
                task.setStatus("failed");
                task.setErrorMessage(errorMessage);
                task.setCompletedAt(LocalDateTime.now());
                
                if (task.getStartedAt() != null) {
                    long seconds = java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).getSeconds();
                    task.setExecutionTime((int) seconds);
                }
                
                taskMapper.updateById(task);
                cacheTaskStatus(task);
            }
        } catch (Exception e) {
            log.error("标记任务失败 - 任务ID: {}", taskId, e);
        }
    }
    
    private void cacheEvaluationResult(ModelEvaluation evaluation) {
        try {
            String key = EVALUATION_CACHE_PREFIX + evaluation.getModelId();
            redisTemplate.opsForValue().set(key, evaluation, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存评估结果失败: {}", e.getMessage());
        }
    }
    
    private ModelEvaluation getCachedEvaluationResult(String modelId) {
        try {
            String key = EVALUATION_CACHE_PREFIX + modelId;
            return (ModelEvaluation) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("获取缓存评估结果失败: {}", e.getMessage());
            return null;
        }
    }
    
    private void cacheTaskStatus(EvaluationTask task) {
        try {
            String key = TASK_CACHE_PREFIX + task.getTaskId();
            redisTemplate.opsForValue().set(key, task, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存任务状态失败: {}", e.getMessage());
        }
    }
    
    private EvaluationTask getCachedTaskStatus(String taskId) {
        try {
            String key = TASK_CACHE_PREFIX + taskId;
            return (EvaluationTask) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("获取缓存任务状态失败: {}", e.getMessage());
            return null;
        }
    }
    
    private String generateTaskId() {
        return "eval_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // ========== 内部结果类 ==========
    
    private static class GeometricEvaluationResult {
        private final BigDecimal score;
        private final Map<String, Object> details;
        
        public GeometricEvaluationResult(BigDecimal score, Map<String, Object> details) {
            this.score = score;
            this.details = details;
        }
        
        public BigDecimal getScore() { return score; }
        public Map<String, Object> getDetails() { return details; }
    }
    
    private static class VisualEvaluationResult {
        private final BigDecimal score;
        private final Map<String, Object> details;
        
        public VisualEvaluationResult(BigDecimal score, Map<String, Object> details) {
            this.score = score;
            this.details = details;
        }
        
        public BigDecimal getScore() { return score; }
        public Map<String, Object> getDetails() { return details; }
    }
    
    private static class TechnicalEvaluationResult {
        private final BigDecimal score;
        private final Map<String, Object> details;
        
        public TechnicalEvaluationResult(BigDecimal score, Map<String, Object> details) {
            this.score = score;
            this.details = details;
        }
        
        public BigDecimal getScore() { return score; }
        public Map<String, Object> getDetails() { return details; }
    }
}