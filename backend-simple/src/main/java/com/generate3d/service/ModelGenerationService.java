package com.generate3d.service;

import com.alibaba.fastjson2.JSON;
import com.generate3d.dto.GenerateModelRequest;
import com.generate3d.dto.ImageTo3DRequest;
import com.generate3d.dto.ModelResponse;
import com.generate3d.entity.GenerationTask;
import com.generate3d.entity.Model;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final ImageProcessingService imageProcessingService;
    
    /**
     * 生成3D模型（新接口）
     */
    public ModelResponse generateModel(GenerateModelRequest request) {
        try {
            // 1. 转换参数
            Map<String, Object> parameters = convertRequestToParams(request);
            
            // 2. 创建生成任务
            GenerationTask task = createGenerationTask(request.getText(), parameters);
            
            // 3. 构建响应
            ModelResponse response = new ModelResponse();
            response.setTaskId(task.getTaskId());
            response.setStatus(task.getStatus());
            response.setProgress(task.getProgress());
            response.setEstimatedTime(task.getEstimatedTime());
            response.setGenerationParams(task.getGenerationParams());
            
            return response;
            
        } catch (Exception e) {
            log.error("模型生成失败", e);
            throw new RuntimeException("模型生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 图片生成3D模型
     */
    public ModelResponse generateModelFromImage(ImageTo3DRequest request) {
        try {
            // 1. 上传图片到存储服务
            String imageUrl = uploadImageFile(request.getImage());
            
            // 2. 转换参数
            Map<String, Object> parameters = convertImageRequestToParams(request, imageUrl);
            
            // 3. 创建生成任务
            GenerationTask task = createImageGenerationTask(imageUrl, parameters);
            
            // 4. 构建响应
            ModelResponse response = new ModelResponse();
            response.setTaskId(task.getTaskId());
            response.setStatus(task.getStatus());
            response.setProgress(task.getProgress());
            response.setEstimatedTime(task.getEstimatedTime());
            response.setGenerationParams(task.getGenerationParams());
            
            return response;
            
        } catch (Exception e) {
            log.error("图片生成3D模型失败", e);
            throw new RuntimeException("图片生成3D模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 转换请求参数
     */
    private Map<String, Object> convertRequestToParams(GenerateModelRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("length", request.getLength());
        params.put("width", request.getWidth());
        params.put("height", request.getHeight());
        params.put("style", request.getStyle());
        params.put("text", request.getText());
        return params;
    }
    
    /**
     * 转换图片请求参数
     */
    private Map<String, Object> convertImageRequestToParams(ImageTo3DRequest request, String imageUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        params.put("length", request.getLength());
        params.put("width", request.getWidth());
        params.put("height", request.getHeight());
        params.put("style", request.getStyle());
        params.put("quality", request.getQuality());
        params.put("preserveDetails", request.getPreserveDetails());
        params.put("originalFilename", request.getImage().getOriginalFilename());
        return params;
    }
    
    /**
     * 上传图片文件
     */
    private String uploadImageFile(MultipartFile imageFile) {
        try {
            // 1. 预处理图片（验证、调整尺寸、格式转换）
            byte[] processedImageBytes = imageProcessingService.preprocessImage(imageFile);
            
            // 2. 获取图片信息
            ImageProcessingService.ImageInfo imageInfo = imageProcessingService.getImageInfo(imageFile);
            log.info("图片信息: {}", imageInfo);
            
            // 3. 生成唯一文件名
            String originalFilename = imageFile.getOriginalFilename();
            String fileName = "image_" + System.currentTimeMillis() + ".jpg"; // 统一使用jpg格式
            
            // 4. 上传处理后的图片到存储服务
            String imageUrl = storageService.uploadFile("images/" + fileName, processedImageBytes);
            
            log.info("图片预处理并上传成功: {} -> {}", originalFilename, imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("图片预处理失败", e);
            throw new RuntimeException("图片预处理失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建图片生成3D模型任务
     */
    public GenerationTask createImageGenerationTask(String imageUrl, Map<String, Object> parameters) {
        // 1. 验证输入
        validateImageInput(imageUrl, parameters);
        
        // 2. 创建任务记录
        GenerationTask task = new GenerationTask();
        task.setTaskId(generateTaskId());
        task.setInputText("图片生成: " + parameters.get("originalFilename"));
        task.setGenerationParams(JSON.toJSONString(parameters));
        task.setStatus("pending");
        task.setProgress(0);
        task.setEstimatedTime(180); // 图片生成预估3分钟
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // 3. 保存任务
        taskService.save(task);
        
        // 4. 异步执行生成
        executeImageGenerationAsync(task.getTaskId());
        
        log.info("创建图片生成3D模型任务: {}", task.getTaskId());
        return task;
    }
    
    /**
     * 验证图片输入参数
     */
    private void validateImageInput(String imageUrl, Map<String, Object> parameters) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("图片URL不能为空");
        }
        
        if (parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException("生成参数不能为空");
        }
        
        // 验证必要参数
        if (!parameters.containsKey("length") || !parameters.containsKey("width") || !parameters.containsKey("height")) {
            throw new IllegalArgumentException("尺寸参数不完整");
        }
    }
    
    /**
     * 异步执行图片生成3D模型
     */
    @Async
    public void executeImageGenerationAsync(String taskId) {
        try {
            log.info("开始执行图片生成3D模型任务: {}", taskId);
            
            // 1. 获取任务信息
            GenerationTask task = taskService.getByTaskId(taskId);
            if (task == null) {
                log.error("任务不存在: {}", taskId);
                return;
            }
            
            // 2. 更新任务状态为处理中
            updateTaskStatus(taskId, "processing", 10);
            
            // 3. 解析参数
            Map<String, Object> params = JSON.parseObject(task.getGenerationParams(), Map.class);
            String imageUrl = (String) params.get("imageUrl");
            
            // 4. 调用AI服务生成3D模型
            updateTaskStatus(taskId, "processing", 30);
            String modelData = generate3DModelFromImage(imageUrl, params);
            
            // 5. 生成模型ID并上传文件
            updateTaskStatus(taskId, "processing", 70);
            String modelId = generateModelId();
            String modelFilePath = uploadModelFile(modelId, modelData);
            
            // 6. 生成缩略图
            updateTaskStatus(taskId, "processing", 85);
            String thumbnailPath = generateThumbnail(modelId, modelData);
            
            // 7. 创建模型记录
            updateTaskStatus(taskId, "processing", 95);
            createImageModelRecord(task, modelId, params, modelFilePath, thumbnailPath);
            
            // 8. 完成任务
            completeTask(taskId, modelId);
            
            log.info("图片生成3D模型任务完成: {}, 模型ID: {}", taskId, modelId);
            
        } catch (Exception e) {
            log.error("图片生成3D模型任务执行失败: {}", taskId, e);
            failTask(taskId, e.getMessage());
        }
    }
    
    /**
     * 调用AI服务从图片生成3D模型
     */
    private String generate3DModelFromImage(String imageUrl, Map<String, Object> params) {
        try {
            // 调用混元AI的图片生成3D模型接口
            String result = hunyuanService.generateModelFromImage(imageUrl, params);
            
            if (!StringUtils.hasText(result)) {
                throw new RuntimeException("AI服务返回空结果");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("调用AI服务生成3D模型失败", e);
            // 如果AI服务失败，返回一个简单的立方体作为fallback
            return generateSimpleCube("#888888", "medium", "图片生成的3D模型");
        }
    }
    
    /**
     * 创建图片模型记录
     */
    private Model createImageModelRecord(GenerationTask task, String modelId, Map<String, Object> params, 
                                        String modelFilePath, String thumbnailPath) {
        Model model = new Model();
        model.setModelId(modelId);
        model.setName(generateImageModelName((String) params.get("originalFilename")));
        model.setDescription("基于图片生成的3D模型");
        model.setFilePath(modelFilePath);
        model.setThumbnailPath(thumbnailPath);
        model.setFileSize(1024L); // 默认大小
        model.setFileFormat("obj");
        model.setVerticesCount(extractVerticesCount("default"));
        model.setFacesCount(extractFacesCount("default"));
        model.setBoundingBoxMin(String.format("%.1f,%.1f,%.1f", 0.0, 0.0, 0.0));
        model.setBoundingBoxMax(String.format("%.1f,%.1f,%.1f", 
            getDoubleParam(params, "length", 6.0),
            getDoubleParam(params, "width", 4.0), 
            getDoubleParam(params, "height", 3.0)));
        model.setGenerationParams(task.getGenerationParams());
        model.setMaterialType(getMaterialByStyle((String) params.get("style")));
        model.setPrimaryColor(getColorByStyle((String) params.get("style")));
        model.setModelSize("medium");
        model.setStatus(1); // 1-正常
        model.setFavorite(0); // 0-未收藏
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        
        return modelService.saveModel(model);
    }
    
    /**
     * 生成图片模型名称
     */
    private String generateImageModelName(String originalFilename) {
        if (StringUtils.hasText(originalFilename)) {
            String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            return "图片模型_" + nameWithoutExt;
        }
        return "图片生成的3D模型_" + System.currentTimeMillis();
    }
    
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
        
        // 获取新的参数结构
        Double length = getDoubleParam(params, "length", 1.0);
        Double width = getDoubleParam(params, "width", 1.0);
        Double height = getDoubleParam(params, "height", 1.0);
        String style = (String) params.getOrDefault("style", "realistic");
        
        // 兼容旧参数
        String color = (String) params.getOrDefault("color", "#ff0000");
        String size = (String) params.getOrDefault("size", "medium");
        
        // 如果有新参数，使用新的生成方法
        if (params.containsKey("length") || params.containsKey("width") || params.containsKey("height")) {
            return generateModelWithDimensions(length, width, height, style, prompt);
        } else {
            // 兼容旧方法
            return generateSimpleCube(color, size, prompt);
        }
    }
    
    /**
     * 根据具体尺寸生成模型
     */
    private String generateModelWithDimensions(Double length, Double width, Double height, String style, String description) {
        // 根据风格调整模型特征
        String materialType = getMaterialByStyle(style);
        String colorScheme = getColorByStyle(style);
        
        return String.format("{"
                + "  \"asset\": { \"version\": \"2.0\" },"
                + "  \"scene\": 0,"
                + "  \"scenes\": [{ \"nodes\": [0] }],"
                + "  \"nodes\": [{ \"mesh\": 0 }],"
                + "  \"meshes\": [{"
                + "    \"primitives\": [{"
                + "      \"attributes\": { \"POSITION\": 0 },"
                + "      \"indices\": 1"
                + "    }]"
                + "  }],"
                + "  \"accessors\": ["
                + "    {"
                + "      \"bufferView\": 0,"
                + "      \"componentType\": 5126,"
                + "      \"count\": 8,"
                + "      \"type\": \"VEC3\","
                + "      \"max\": [%.2f, %.2f, %.2f],"
                + "      \"min\": [-%.2f, -%.2f, -%.2f]"
                + "    }"
                + "  ],"
                + "  \"description\": \"%s\","
                + "  \"style\": \"%s\","
                + "  \"material\": \"%s\","
                + "  \"dimensions\": { \"length\": %.2f, \"width\": %.2f, \"height\": %.2f }"
                + "}", 
                length/2, width/2, height/2, length/2, width/2, height/2, 
                description, style, materialType, length, width, height);
    }
    
    /**
     * 根据风格获取材质
     */
    private String getMaterialByStyle(String style) {
        switch (style.toLowerCase()) {
            case "cartoon": return "toon";
            case "realistic": return "pbr";
            case "minimalist": return "basic";
            case "industrial": return "metal";
            case "organic": return "organic";
            default: return "standard";
        }
    }
    
    /**
     * 根据风格获取颜色方案
     */
    private String getColorByStyle(String style) {
        switch (style.toLowerCase()) {
            case "cartoon": return "#ff6b6b";
            case "realistic": return "#8b7355";
            case "minimalist": return "#ffffff";
            case "industrial": return "#4a4a4a";
            case "organic": return "#90ee90";
            default: return "#cccccc";
        }
    }
    
    /**
     * 安全获取Double参数
     */
    private Double getDoubleParam(Map<String, Object> params, String key, Double defaultValue) {
        Object value = params.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("参数 {} 格式错误: {}, 使用默认值: {}", key, value, defaultValue);
            return defaultValue;
        }
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