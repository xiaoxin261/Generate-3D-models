package com.generate3d.service;

import com.alibaba.fastjson2.JSON;
import com.generate3d.entity.GenerationTask;
import com.generate3d.entity.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 模型生成服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelGenerationService {
    
    private final GenerationTaskService taskService;
    private final ModelService modelService;
    private final HunyuanService hunyuanService;
    private final StorageService storageService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 创建3D模型生成任务
     */
    public GenerationTask createGenerationTask(String inputText, Map<String, Object> parameters) {
        // 1. 验证输入
        validateInput(inputText, parameters);
        
        // 2. 创建任务
        String taskId = generateTaskId();
        GenerationTask task = new GenerationTask();
        task.setTaskId(taskId);
        task.setInputText(inputText);
        task.setGenerationParams(JSON.toJSONString(parameters));
        task.setStatus("pending");
        task.setProgress(0);
        task.setEstimatedTime(25);
        
        taskService.save(task);
        
        // 3. 缓存任务状态
        cacheTaskStatus(task);
        
        // 4. 异步执行生成
        executeGenerationAsync(taskId);
        
        return task;
    }
    
    /**
     * 获取任务状态
     */
    public GenerationTask getTaskStatus(String taskId) {
        // 先从缓存获取
        GenerationTask cachedTask = getCachedTaskStatus(taskId);
        if (cachedTask != null) {
            return cachedTask;
        }
        
        // 从数据库获取
        return taskService.getByTaskId(taskId);
    }
    
    /**
     * 异步执行模型生成
     */
    @Async
    public void executeGenerationAsync(String taskId) {
        GenerationTask task = taskService.getByTaskId(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        try {
            log.info("开始执行模型生成任务: {}", taskId);
            
            // 1. 更新任务状态为处理中
            updateTaskStatus(taskId, "processing", 10);
            task.setStartedAt(LocalDateTime.now());
            taskService.updateById(task);
            
            // 2. 调用AI服务生成3D模型参数
            String modelPrompt = hunyuanService.generate3DModelPrompt(task.getInputText());
            updateTaskStatus(taskId, "processing", 30);
            
            // 3. 生成3D模型文件
            Map<String, Object> params = JSON.parseObject(task.getGenerationParams(), Map.class);
            String modelData = generate3DModel(modelPrompt, params);
            updateTaskStatus(taskId, "processing", 70);
            
            // 4. 创建模型记录
            String modelId = generateModelId();
            Model model = createModelRecord(task, modelId, params);
            modelService.saveModel(model);
            
            // 5. 上传到存储服务
            String filePath = uploadModelFile(modelId, modelData);
            updateTaskStatus(taskId, "processing", 90);
            
            // 6. 生成缩略图
            String thumbnailPath = generateThumbnail(modelId, modelData);
            
            // 7. 更新模型文件信息
            model.setFilePath(filePath);
            model.setThumbnailPath(thumbnailPath);
            model.setFileSize((long) modelData.length());
            model.setVerticesCount(extractVerticesCount(modelData));
            model.setFacesCount(extractFacesCount(modelData));
            modelService.updateById(model);
            
            // 8. 完成任务
            completeTask(taskId, modelId);
            
            log.info("模型生成任务完成: {}, 模型ID: {}", taskId, modelId);
            
        } catch (Exception e) {
            log.error("模型生成失败: {}", taskId, e);
            failTask(taskId, e.getMessage());
        }
    }
    
    /**
     * 生成3D模型数据
     */
    private String generate3DModel(String prompt, Map<String, Object> params) {
        // 这里实现具体的3D模型生成逻辑
        // 可以调用第三方3D生成API或使用本地算法
        
        // 示例：生成简单的立方体GLTF数据
        String color = (String) params.getOrDefault("color", "#ff0000");
        String size = (String) params.getOrDefault("size", "medium");
        
        return generateSimpleCube(color, size, prompt);
    }
    
    /**
     * 生成简单立方体（示例实现）
     */
    private String generateSimpleCube(String color, String size, String description) {
        // 根据参数生成GLTF格式的立方体数据
        double sizeValue = getSizeValue(size);
        
        return String.format("{" +
                "  \"asset\": { \"version\": \"2.0\" }," +
                "  \"scene\": 0," +
                "  \"scenes\": [{ \"nodes\": [0] }]," +
                "  \"nodes\": [{ \"mesh\": 0 }]," +
                "  \"meshes\": [{" +
                "    \"primitives\": [{" +
                "      \"attributes\": { \"POSITION\": 0 }," +
                "      \"indices\": 1" +
                "    }]" +
                "  }]," +
                "  \"accessors\": [" +
                "    {" +
                "      \"bufferView\": 0," +
                "      \"componentType\": 5126," +
                "      \"count\": 8," +
                "      \"type\": \"VEC3\"," +
                "      \"max\": [%.2f, %.2f, %.2f]," +
                "      \"min\": [-%.2f, -%.2f, -%.2f]" +
                "    }" +
                "  ]," +
                "  \"description\": \"%s\"," +
                "  \"color\": \"%s\"" +
                "}", sizeValue, sizeValue, sizeValue, sizeValue, sizeValue, sizeValue, description, color);
    }
    
    /**
     * 获取尺寸数值
     */
    private double getSizeValue(String size) {
        switch (size.toLowerCase()) {
            case "small": return 0.5;
            case "large": return 2.0;
            default: return 1.0; // medium
        }
    }
    
    /**
     * 创建模型记录
     */
    private Model createModelRecord(GenerationTask task, String modelId, Map<String, Object> params) {
        Model model = new Model();
        model.setModelId(modelId);
        model.setName(generateModelName(task.getInputText()));
        model.setDescription(task.getInputText());
        model.setOriginalText(task.getInputText());
        model.setCategory("generated");
        model.setFileFormat("gltf");
        model.setGenerationParams(task.getGenerationParams());
        model.setMaterialType((String) params.getOrDefault("material", "plastic"));
        model.setPrimaryColor((String) params.getOrDefault("color", "#ff0000"));
        model.setModelSize((String) params.getOrDefault("size", "medium"));
        model.setStatus(1);
        model.setFavorite(0);
        
        return model;
    }
    
    /**
     * 生成模型名称
     */
    private String generateModelName(String inputText) {
        if (inputText.length() > 20) {
            return inputText.substring(0, 20) + "...";
        }
        return inputText;
    }
    
    /**
     * 上传模型文件
     */
    private String uploadModelFile(String modelId, String modelData) {
        String fileName = modelId + ".gltf";
        return storageService.uploadFile(fileName, modelData.getBytes());
    }
    
    /**
     * 生成缩略图
     */
    private String generateThumbnail(String modelId, String modelData) {
        // 这里应该实现3D模型缩略图生成逻辑
        // 暂时返回默认缩略图路径
        return "thumbnails/default.png";
    }
    
    /**
     * 提取顶点数量
     */
    private Integer extractVerticesCount(String modelData) {
        // 从模型数据中提取顶点数量
        // 这里返回默认值
        return 8; // 立方体有8个顶点
    }
    
    /**
     * 提取面数量
     */
    private Integer extractFacesCount(String modelData) {
        // 从模型数据中提取面数量
        // 这里返回默认值
        return 12; // 立方体有12个三角面
    }
    
    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, String status, int progress) {
        GenerationTask task = new GenerationTask();
        task.setTaskId(taskId);
        task.setStatus(status);
        task.setProgress(progress);
        
        taskService.updateStatusByTaskId(taskId, status, progress);
        
        // 更新缓存
        cacheTaskStatus(task);
    }
    
    /**
     * 完成任务
     */
    private void completeTask(String taskId, String modelId) {
        LocalDateTime completedAt = LocalDateTime.now();
        GenerationTask task = taskService.getByTaskId(taskId);
        
        int actualTime = 0;
        if (task.getStartedAt() != null) {
            actualTime = (int) Duration.between(task.getStartedAt(), completedAt).getSeconds();
        }
        
        taskService.completeTask(taskId, modelId, completedAt, actualTime);
        
        // 更新缓存
        task.setStatus("completed");
        task.setProgress(100);
        task.setModelId(modelId);
        task.setCompletedAt(completedAt);
        task.setActualTime(actualTime);
        cacheTaskStatus(task);
    }
    
    /**
     * 任务失败
     */
    private void failTask(String taskId, String errorMessage) {
        taskService.failTask(taskId, errorMessage);
        
        // 更新缓存
        GenerationTask task = new GenerationTask();
        task.setTaskId(taskId);
        task.setStatus("failed");
        task.setErrorMessage(errorMessage);
        cacheTaskStatus(task);
    }
    
    /**
     * 缓存任务状态
     */
    private void cacheTaskStatus(GenerationTask task) {
        String key = "task:" + task.getTaskId();
        redisTemplate.opsForValue().set(key, task, Duration.ofHours(1));
    }
    
    /**
     * 获取缓存的任务状态
     */
    private GenerationTask getCachedTaskStatus(String taskId) {
        String key = "task:" + taskId;
        return (GenerationTask) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 验证输入
     */
    private void validateInput(String inputText, Map<String, Object> parameters) {
        if (!StringUtils.hasText(inputText)) {
            throw new IllegalArgumentException("输入文本不能为空");
        }
        
        if (inputText.length() < 10 || inputText.length() > 500) {
            throw new IllegalArgumentException("输入文本长度必须在10-500字符之间");
        }
        
        if (parameters == null) {
            throw new IllegalArgumentException("生成参数不能为空");
        }
    }
    
    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成模型ID
     */
    private String generateModelId() {
        return "model_" + UUID.randomUUID().toString().replace("-", "");
    }
}