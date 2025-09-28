package com.generate3d.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.generate3d.common.Result;
import com.generate3d.entity.EvaluationTask;
import com.generate3d.entity.ModelEvaluation;
import com.generate3d.entity.ModelUserRating;
import com.generate3d.service.ModelEvaluationService;
import com.generate3d.service.UserRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 模型评估控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/evaluation")
@Tag(name = "模型评估", description = "3D模型质量评估相关接口")
@RequiredArgsConstructor
public class EvaluationController {
    
    private final ModelEvaluationService evaluationService;
    private final UserRatingService ratingService;
    
    /**
     * 创建模型评估任务
     */
    @PostMapping("/tasks")
    @Operation(summary = "创建评估任务", description = "为指定模型创建质量评估任务",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<String> createEvaluationTask(
            @RequestParam @NotBlank(message = "模型ID不能为空") String modelId,
            @RequestParam(defaultValue = "FULL") String evaluationType,
            @RequestParam(defaultValue = "MEDIUM") String priority) {
        try {
            String taskId = evaluationService.createEvaluationTask(modelId, evaluationType, priority);
            return Result.success(taskId);
        } catch (Exception e) {
            log.error("创建评估任务失败", e);
            return Result.error("创建评估任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评估任务状态
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务状态", description = "获取评估任务的执行状态和进度")
    public Result<EvaluationTask> getTaskStatus(@PathVariable String taskId) {
        try {
            EvaluationTask task = evaluationService.getTaskStatus(taskId);
            if (task != null) {
                return Result.success(task);
            } else {
                return Result.error("任务不存在");
            }
        } catch (Exception e) {
            log.error("获取任务状态失败", e);
            return Result.error("获取任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型评估结果
     */
    @GetMapping("/results/{modelId}")
    @Operation(summary = "获取评估结果", description = "获取指定模型的质量评估结果")
    public Result<ModelEvaluation> getEvaluationResult(@PathVariable String modelId) {
        try {
            ModelEvaluation evaluation = evaluationService.getEvaluationResult(modelId);
            if (evaluation != null) {
                return Result.success(evaluation);
            } else {
                return Result.error("评估结果不存在");
            }
        } catch (Exception e) {
            log.error("获取评估结果失败", e);
            return Result.error("获取评估结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型评估历史
     */
    @GetMapping("/history/{modelId}")
    @Operation(summary = "获取评估历史", description = "获取指定模型的历史评估记录")
    public Result<List<ModelEvaluation>> getEvaluationHistory(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        try {
            List<ModelEvaluation> history = evaluationService.getEvaluationHistory(modelId, limit);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取评估历史失败", e);
            return Result.error("获取评估历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新评估模型
     */
    @PostMapping("/re-evaluate/{modelId}")
    @Operation(summary = "重新评估", description = "重新对指定模型进行质量评估",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<String> reEvaluateModel(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "FULL") String evaluationType) {
        try {
            String taskId = evaluationService.createEvaluationTask(modelId, evaluationType, "HIGH");
            return Result.success(taskId);
        } catch (Exception e) {
            log.error("重新评估失败", e);
            return Result.error("重新评估失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交用户评分
     */
    @PostMapping("/ratings")
    @Operation(summary = "提交用户评分", description = "用户对模型进行评分和反馈",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<ModelUserRating> submitRating(@RequestBody @Valid UserRatingRequest request) {
        try {
            ModelUserRating rating = ratingService.submitRating(
                    request.getModelId(),
                    request.getUserId(),
                    request.getOverallRating(),
                    request.getQualityRating(),
                    request.getAccuracyRating(),
                    request.getVisualRating(),
                    request.getFeedbackText(),
                    request.getFeedbackTags(),
                    request.isAnonymous()
            );
            return Result.success(rating);
        } catch (Exception e) {
            log.error("提交用户评分失败", e);
            return Result.error("提交评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户评分
     */
    @GetMapping("/ratings/{modelId}/user/{userId}")
    @Operation(summary = "获取用户评分", description = "获取指定用户对模型的评分",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<ModelUserRating> getUserRating(
            @PathVariable String modelId,
            @PathVariable Long userId) {
        try {
            ModelUserRating rating = ratingService.getUserRating(modelId, userId);
            if (rating != null) {
                return Result.success(rating);
            } else {
                return Result.error("用户尚未评分");
            }
        } catch (Exception e) {
            log.error("获取用户评分失败", e);
            return Result.error("获取用户评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型评分统计
     */
    @GetMapping("/ratings/{modelId}/stats")
    @Operation(summary = "获取评分统计", description = "获取指定模型的用户评分统计数据")
    public Result<Map<String, Object>> getRatingStats(@PathVariable String modelId) {
        try {
            Map<String, Object> stats = ratingService.getModelRatingStats(modelId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取评分统计失败", e);
            return Result.error("获取评分统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型评分列表
     */
    @GetMapping("/ratings/{modelId}")
    @Operation(summary = "获取评分列表", description = "获取指定模型的用户评分列表")
    public Result<IPage<ModelUserRating>> getModelRatings(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "latest") String sortBy) {
        try {
            IPage<ModelUserRating> ratings = ratingService.getModelRatings(modelId, page, size, sortBy);
            return Result.success(ratings);
        } catch (Exception e) {
            log.error("获取评分列表失败", e);
            return Result.error("获取评分列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户评分历史
     */
    @GetMapping("/ratings/user/{userId}/history")
    @Operation(summary = "获取用户评分历史", description = "获取指定用户的评分历史记录",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<IPage<ModelUserRating>> getUserRatingHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            IPage<ModelUserRating> history = ratingService.getUserRatingHistory(userId, page, size);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取用户评分历史失败", e);
            return Result.error("获取用户评分历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户评分
     */
    @DeleteMapping("/ratings/{modelId}/user/{userId}")
    @Operation(summary = "删除用户评分", description = "删除指定用户对模型的评分",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<String> deleteUserRating(
            @PathVariable String modelId,
            @PathVariable Long userId) {
        try {
            boolean success = ratingService.deleteUserRating(modelId, userId);
            if (success) {
                return Result.success("删除评分成功");
            } else {
                return Result.error("评分不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户评分失败", e);
            return Result.error("删除评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门评分标签
     */
    @GetMapping("/ratings/{modelId}/tags")
    @Operation(summary = "获取热门标签", description = "获取指定模型的热门评分标签")
    public Result<List<Map<String, Object>>> getPopularTags(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        try {
            List<Map<String, Object>> tags = ratingService.getPopularFeedbackTags(modelId, limit);
            return Result.success(tags);
        } catch (Exception e) {
            log.error("获取热门标签失败", e);
            return Result.error("获取热门标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评分趋势
     */
    @GetMapping("/ratings/{modelId}/trend")
    @Operation(summary = "获取评分趋势", description = "获取指定模型的评分趋势数据")
    public Result<Map<String, Object>> getRatingTrend(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        try {
            Map<String, Object> trend = ratingService.getRatingTrend(modelId, days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取评分趋势失败", e);
            return Result.error("获取评分趋势失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评估系统概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取系统概览", description = "获取评估系统的整体统计数据",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Result<Map<String, Object>> getSystemOverview() {
        try {
            Map<String, Object> overview = evaluationService.getSystemOverview();
            return Result.success(overview);
        } catch (Exception e) {
            log.error("获取系统概览失败", e);
            return Result.error("获取系统概览失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户评分请求DTO
     */
    public static class UserRatingRequest {
        @NotBlank(message = "模型ID不能为空")
        private String modelId;
        
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "总体评分不能为空")
        @Min(value = 1, message = "评分不能小于1")
        @Max(value = 5, message = "评分不能大于5")
        private BigDecimal overallRating;
        
        @Min(value = 1, message = "质量评分不能小于1")
        @Max(value = 5, message = "质量评分不能大于5")
        private BigDecimal qualityRating;
        
        @Min(value = 1, message = "准确性评分不能小于1")
        @Max(value = 5, message = "准确性评分不能大于5")
        private BigDecimal accuracyRating;
        
        @Min(value = 1, message = "视觉效果评分不能小于1")
        @Max(value = 5, message = "视觉效果评分不能大于5")
        private BigDecimal visualRating;
        
        private String feedbackText;
        private List<String> feedbackTags;
        private boolean anonymous;
        
        // Getters and Setters
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getOverallRating() { return overallRating; }
        public void setOverallRating(BigDecimal overallRating) { this.overallRating = overallRating; }
        
        public BigDecimal getQualityRating() { return qualityRating; }
        public void setQualityRating(BigDecimal qualityRating) { this.qualityRating = qualityRating; }
        
        public BigDecimal getAccuracyRating() { return accuracyRating; }
        public void setAccuracyRating(BigDecimal accuracyRating) { this.accuracyRating = accuracyRating; }
        
        public BigDecimal getVisualRating() { return visualRating; }
        public void setVisualRating(BigDecimal visualRating) { this.visualRating = visualRating; }
        
        public String getFeedbackText() { return feedbackText; }
        public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
        
        public List<String> getFeedbackTags() { return feedbackTags; }
        public void setFeedbackTags(List<String> feedbackTags) { this.feedbackTags = feedbackTags; }
        
        public boolean isAnonymous() { return anonymous; }
        public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    }
}