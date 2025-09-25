package com.generate3d.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.generate3d.common.Result;
import com.generate3d.entity.ExportTask;
import com.generate3d.entity.GenerationTask;
import com.generate3d.entity.Model;
import com.generate3d.service.ModelExportService;
import com.generate3d.service.ModelGenerationService;
import com.generate3d.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 3D模型控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "3D模型管理", description = "3D模型生成、管理、导出相关接口")
public class ModelController {
    
    private final ModelService modelService;
    private final ModelGenerationService generationService;
    private final ModelExportService exportService;
    
    // ==================== 文本输入相关接口 ====================
    
    @PostMapping("/input/validate")
    @Operation(summary = "验证文本输入", description = "验证用户输入的文本描述是否符合要求")
    public Result<Map<String, Object>> validateInput(
            @Parameter(description = "输入文本") @RequestParam String text) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 验证文本长度
        if (!StringUtils.hasText(text)) {
            result.put("valid", false);
            result.put("message", "输入文本不能为空");
            return Result.success(result);
        }
        
        if (text.length() < 10) {
            result.put("valid", false);
            result.put("message", "输入文本长度不能少于10个字符");
            return Result.success(result);
        }
        
        if (text.length() > 500) {
            result.put("valid", false);
            result.put("message", "输入文本长度不能超过500个字符");
            return Result.success(result);
        }
        
        result.put("valid", true);
        result.put("message", "输入文本验证通过");
        result.put("length", text.length());
        
        return Result.success(result);
    }
    
    @GetMapping("/input/suggestions")
    @Operation(summary = "获取输入建议", description = "根据关键词获取输入建议")
    public Result<List<String>> getInputSuggestions(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        
        List<String> suggestions = List.of(
            "一只可爱的小猫咪，坐在草地上",
            "现代风格的椅子，简约设计",
            "古典风格的花瓶，带有精美雕刻",
            "科幻风格的机器人，金属质感",
            "卡通风格的汽车，色彩鲜艳",
            "中式传统建筑，红墙黄瓦",
            "现代办公桌，木质材料",
            "抽象艺术雕塑，几何形状"
        );
        
        // 如果有关键词，可以进行过滤
        if (StringUtils.hasText(keyword)) {
            suggestions = suggestions.stream()
                .filter(s -> s.contains(keyword))
                .toList();
        }
        
        return Result.success(suggestions);
    }
    
    // ==================== 3D模型生成相关接口 ====================
    
    @PostMapping("/generate")
    @Operation(summary = "创建3D模型生成任务", description = "根据文本描述创建3D模型生成任务")
    public Result<GenerationTask> createGenerationTask(
            @Parameter(description = "输入文本") @RequestParam String text,
            @Parameter(description = "材质类型") @RequestParam(defaultValue = "plastic") String material,
            @Parameter(description = "主要颜色") @RequestParam(defaultValue = "#ff0000") String color,
            @Parameter(description = "模型尺寸") @RequestParam(defaultValue = "medium") String size) {
        
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("material", material);
            parameters.put("color", color);
            parameters.put("size", size);
            
            GenerationTask task = generationService.createGenerationTask(text, parameters);
            return Result.success(task);
            
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建生成任务失败", e);
            return Result.error("创建生成任务失败");
        }
    }
    
    @GetMapping("/generate/{taskId}/status")
    @Operation(summary = "查询生成任务状态", description = "根据任务ID查询3D模型生成状态")
    public Result<GenerationTask> getGenerationStatus(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        
        GenerationTask task = generationService.getTaskStatus(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        return Result.success(task);
    }
    
    // ==================== 3D模型预览相关接口 ====================
    
    @GetMapping("/{modelId}/preview")
    @Operation(summary = "获取模型预览数据", description = "获取3D模型的预览数据")
    public Result<Map<String, Object>> getModelPreview(
            @Parameter(description = "模型ID") @PathVariable String modelId) {
        
        Model model = modelService.getByModelId(modelId);
        if (model == null) {
            return Result.error("模型不存在");
        }
        
        Map<String, Object> preview = new HashMap<>();
        preview.put("modelId", model.getModelId());
        preview.put("name", model.getName());
        preview.put("description", model.getDescription());
        preview.put("filePath", model.getFilePath());
        preview.put("thumbnailPath", model.getThumbnailPath());
        preview.put("fileFormat", model.getFileFormat());
        preview.put("verticesCount", model.getVerticesCount());
        preview.put("facesCount", model.getFacesCount());
        preview.put("boundingBox", model.getBoundingBox());
        preview.put("primaryColor", model.getPrimaryColor());
        preview.put("materialType", model.getMaterialType());
        
        return Result.success(preview);
    }
    
    @GetMapping("/{modelId}/file")
    @Operation(summary = "获取模型文件", description = "获取3D模型文件的下载链接")
    public Result<Map<String, String>> getModelFile(
            @Parameter(description = "模型ID") @PathVariable String modelId) {
        
        Model model = modelService.getByModelId(modelId);
        if (model == null) {
            return Result.error("模型不存在");
        }
        
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("filePath", model.getFilePath());
        fileInfo.put("fileName", model.getModelId() + "." + model.getFileFormat());
        fileInfo.put("fileFormat", model.getFileFormat());
        fileInfo.put("fileSize", String.valueOf(model.getFileSize()));
        
        return Result.success(fileInfo);
    }
    
    // ==================== 模型管理相关接口 ====================
    
    @GetMapping
    @Operation(summary = "获取模型列表", description = "分页获取3D模型列表")
    public Result<Page<Model>> getModelList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        
        Page<Model> modelPage = modelService.getModelList(page, size, category, keyword);
        return Result.success(modelPage);
    }
    
    @GetMapping("/{modelId}")
    @Operation(summary = "获取模型详情", description = "根据模型ID获取详细信息")
    public Result<Model> getModelDetail(
            @Parameter(description = "模型ID") @PathVariable String modelId) {
        
        Model model = modelService.getByModelId(modelId);
        if (model == null) {
            return Result.error("模型不存在");
        }
        
        return Result.success(model);
    }
    
    @GetMapping("/favorites")
    @Operation(summary = "获取收藏模型", description = "分页获取用户收藏的模型")
    public Result<Page<Model>> getFavoriteModels(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Page<Model> modelPage = modelService.getFavoriteModels(page, size);
        return Result.success(modelPage);
    }
    
    @PostMapping("/{modelId}/favorite")
    @Operation(summary = "收藏模型", description = "添加或取消收藏模型")
    public Result<String> toggleFavorite(
            @Parameter(description = "模型ID") @PathVariable String modelId,
            @Parameter(description = "是否收藏") @RequestParam boolean favorite) {
        
        boolean success = modelService.toggleFavorite(modelId, favorite);
        if (!success) {
            return Result.error("操作失败");
        }
        
        return Result.success(favorite ? "已收藏" : "已取消收藏");
    }
    
    @DeleteMapping("/{modelId}")
    @Operation(summary = "删除模型", description = "软删除指定的3D模型")
    public Result<String> deleteModel(
            @Parameter(description = "模型ID") @PathVariable String modelId) {
        
        boolean success = modelService.deleteModel(modelId);
        if (!success) {
            return Result.error("删除失败");
        }
        
        return Result.success("删除成功");
    }
    
    @PostMapping("/{modelId}/regenerate")
    @Operation(summary = "重新生成模型", description = "基于原始文本重新生成3D模型")
    public Result<GenerationTask> regenerateModel(
            @Parameter(description = "模型ID") @PathVariable String modelId,
            @Parameter(description = "新的生成参数") @RequestBody(required = false) Map<String, Object> newParams) {
        
        try {
            Model model = modelService.getByModelId(modelId);
            if (model == null) {
                return Result.error("模型不存在");
            }
            
            // 使用原始文本和新参数重新生成
            Map<String, Object> parameters = newParams != null ? newParams : 
                JSON.parseObject(model.getGenerationParams(), Map.class);
            
            GenerationTask task = generationService.createGenerationTask(model.getOriginalText(), parameters);
            return Result.success(task);
            
        } catch (Exception e) {
            log.error("重新生成模型失败", e);
            return Result.error("重新生成失败");
        }
    }
    
    // ==================== 模型导出相关接口 ====================
    
    @PostMapping("/{modelId}/export")
    @Operation(summary = "导出单个模型", description = "导出指定格式的3D模型文件")
    public Result<ExportTask> exportSingleModel(
            @Parameter(description = "模型ID") @PathVariable String modelId,
            @Parameter(description = "导出格式") @RequestParam String format,
            @Parameter(description = "包含纹理") @RequestParam(defaultValue = "true") boolean includeTextures,
            @Parameter(description = "包含材质") @RequestParam(defaultValue = "true") boolean includeMaterials) {
        
        try {
            ExportTask task = exportService.createSingleExportTask(modelId, format, includeTextures, includeMaterials);
            return Result.success(task);
            
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建导出任务失败", e);
            return Result.error("创建导出任务失败");
        }
    }
    
    @PostMapping("/export/batch")
    @Operation(summary = "批量导出模型", description = "批量导出多个3D模型")
    public Result<ExportTask> exportBatchModels(
            @Parameter(description = "模型ID列表") @RequestParam List<String> modelIds,
            @Parameter(description = "导出格式") @RequestParam String format,
            @Parameter(description = "包含纹理") @RequestParam(defaultValue = "true") boolean includeTextures,
            @Parameter(description = "包含材质") @RequestParam(defaultValue = "true") boolean includeMaterials) {
        
        try {
            ExportTask task = exportService.createBatchExportTask(modelIds, format, includeTextures, includeMaterials);
            return Result.success(task);
            
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建批量导出任务失败", e);
            return Result.error("创建批量导出任务失败");
        }
    }
    
    @GetMapping("/export/{exportId}/status")
    @Operation(summary = "查询导出状态", description = "根据导出ID查询导出任务状态")
    public Result<ExportTask> getExportStatus(
            @Parameter(description = "导出ID") @PathVariable String exportId) {
        
        ExportTask task = exportService.getExportStatus(exportId);
        if (task == null) {
            return Result.error("导出任务不存在");
        }
        
        return Result.success(task);
    }
    
    @GetMapping("/export/{exportId}/download")
    @Operation(summary = "下载导出文件", description = "获取导出文件的下载链接")
    public Result<Map<String, String>> downloadExportFile(
            @Parameter(description = "导出ID") @PathVariable String exportId) {
        
        ExportTask task = exportService.getExportStatus(exportId);
        if (task == null) {
            return Result.error("导出任务不存在");
        }
        
        if (!"completed".equals(task.getStatus())) {
            return Result.error("导出任务未完成");
        }
        
        Map<String, String> downloadInfo = new HashMap<>();
        downloadInfo.put("downloadUrl", task.getDownloadUrl());
        downloadInfo.put("fileName", exportId + "." + task.getExportFormat());
        downloadInfo.put("fileSize", String.valueOf(task.getFileSize()));
        downloadInfo.put("expirationTime", task.getExpirationTime().toString());
        
        return Result.success(downloadInfo);
    }
    
    // ==================== 系统相关接口 ====================
    
    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "检查系统运行状态")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        
        return Result.success(health);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取系统统计", description = "获取模型生成和导出的统计信息")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 模型统计
        Map<String, Object> modelStats = modelService.getModelStatistics();
        statistics.put("models", modelStats);
        
        return Result.success(statistics);
    }
}