package com.generate3d.service;

import com.alibaba.fastjson2.JSON;
import com.generate3d.entity.ExportTask;
import com.generate3d.entity.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 模型导出服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelExportService {
    
    private final ExportTaskService exportTaskService;
    private final ModelService modelService;
    private final StorageService storageService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 创建单个模型导出任务
     */
    public ExportTask createSingleExportTask(String modelId, String format, 
                                            boolean includeTextures, boolean includeMaterials) {
        // 验证模型存在
        Model model = modelService.getByModelId(modelId);
        if (model == null) {
            throw new IllegalArgumentException("模型不存在: " + modelId);
        }
        
        // 创建导出任务
        String exportId = generateExportId();
        ExportTask task = new ExportTask();
        task.setExportId(exportId);
        task.setModelIds(JSON.toJSONString(List.of(modelId)));
        task.setExportFormat(format);
        task.setIncludeTextures(includeTextures ? 1 : 0);
        task.setIncludeMaterials(includeMaterials ? 1 : 0);
        task.setStatus("pending");
        task.setProgress(0);
        task.setExpirationTime(LocalDateTime.now().plusDays(7)); // 7天后过期
        
        exportTaskService.save(task);
        
        // 缓存任务状态
        cacheExportStatus(task);
        
        // 异步执行导出
        executeExportAsync(exportId);
        
        return task;
    }
    
    /**
     * 创建批量模型导出任务
     */
    public ExportTask createBatchExportTask(List<String> modelIds, String format,
                                          boolean includeTextures, boolean includeMaterials) {
        if (CollectionUtils.isEmpty(modelIds)) {
            throw new IllegalArgumentException("模型ID列表不能为空");
        }
        
        // 验证所有模型存在
        for (String modelId : modelIds) {
            Model model = modelService.getByModelId(modelId);
            if (model == null) {
                throw new IllegalArgumentException("模型不存在: " + modelId);
            }
        }
        
        // 创建导出任务
        String exportId = generateExportId();
        ExportTask task = new ExportTask();
        task.setExportId(exportId);
        task.setModelIds(JSON.toJSONString(modelIds));
        task.setExportFormat(format);
        task.setIncludeTextures(includeTextures ? 1 : 0);
        task.setIncludeMaterials(includeMaterials ? 1 : 0);
        task.setStatus("pending");
        task.setProgress(0);
        task.setExpirationTime(LocalDateTime.now().plusDays(7));
        
        exportTaskService.save(task);
        
        // 缓存任务状态
        cacheExportStatus(task);
        
        // 异步执行导出
        executeExportAsync(exportId);
        
        return task;
    }
    
    /**
     * 获取导出任务状态
     */
    public ExportTask getExportStatus(String exportId) {
        // 先从缓存获取
        ExportTask cachedTask = getCachedExportStatus(exportId);
        if (cachedTask != null) {
            return cachedTask;
        }
        
        // 从数据库获取
        return exportTaskService.getByExportId(exportId);
    }
    
    /**
     * 异步执行导出
     */
    @Async
    public void executeExportAsync(String exportId) {
        ExportTask task = exportTaskService.getByExportId(exportId);
        if (task == null) {
            log.error("导出任务不存在: {}", exportId);
            return;
        }
        
        try {
            log.info("开始执行模型导出任务: {}", exportId);
            
            // 1. 更新任务状态为处理中
            updateExportStatus(exportId, "processing", 10);
            
            // 2. 获取模型列表
            List<String> modelIds = JSON.parseArray(task.getModelIds(), String.class);
            List<Model> models = modelService.getModelsByIds(modelIds);
            updateExportStatus(exportId, "processing", 30);
            
            // 3. 转换模型格式
            Map<String, byte[]> convertedFiles = convertModels(models, task.getExportFormat(),
                    task.getIncludeTextures() == 1, task.getIncludeMaterials() == 1);
            updateExportStatus(exportId, "processing", 70);
            
            // 4. 打包文件
            byte[] zipData = null;
            String outputPath;
            long fileSize;
            
            if (convertedFiles.size() == 1 && modelIds.size() == 1) {
                // 单个文件直接上传
                String fileName = modelIds.get(0) + "." + task.getExportFormat();
                byte[] fileData = convertedFiles.values().iterator().next();
                outputPath = storageService.uploadFile("exports/" + fileName, fileData);
                fileSize = fileData.length;
            } else {
                // 多个文件打包成ZIP
                zipData = createZipArchive(convertedFiles);
                String zipFileName = exportId + ".zip";
                outputPath = storageService.uploadFile("exports/" + zipFileName, zipData);
                fileSize = zipData.length;
            }
            
            updateExportStatus(exportId, "processing", 90);
            
            // 5. 生成下载URL
            String downloadUrl = storageService.generateDownloadUrl(outputPath);
            
            // 6. 完成任务
            completeExportTask(exportId, outputPath, fileSize, downloadUrl);
            
            log.info("模型导出任务完成: {}", exportId);
            
        } catch (Exception e) {
            log.error("模型导出失败: {}", exportId, e);
            failExportTask(exportId, e.getMessage());
        }
    }
    
    /**
     * 转换模型格式
     */
    private Map<String, byte[]> convertModels(List<Model> models, String targetFormat,
                                            boolean includeTextures, boolean includeMaterials) {
        Map<String, byte[]> convertedFiles = new java.util.HashMap<>();
        
        for (Model model : models) {
            try {
                // 获取原始模型文件
                byte[] originalData = storageService.downloadFile(model.getFilePath());
                
                // 转换格式
                byte[] convertedData = convertModelFormat(originalData, model.getFileFormat(),
                        targetFormat, includeTextures, includeMaterials);
                
                String fileName = model.getModelId() + "." + targetFormat;
                convertedFiles.put(fileName, convertedData);
                
            } catch (Exception e) {
                log.error("转换模型格式失败: {}", model.getModelId(), e);
                throw new RuntimeException("转换模型格式失败: " + model.getModelId(), e);
            }
        }
        
        return convertedFiles;
    }
    
    /**
     * 转换模型格式（具体实现）
     */
    private byte[] convertModelFormat(byte[] originalData, String sourceFormat, String targetFormat,
                                    boolean includeTextures, boolean includeMaterials) {
        // 如果格式相同，直接返回
        if (sourceFormat.equalsIgnoreCase(targetFormat)) {
            return originalData;
        }
        
        // 这里应该实现具体的格式转换逻辑
        // 可以使用第三方库如Assimp、Three.js等
        
        switch (targetFormat.toLowerCase()) {
            case "obj":
                return convertToOBJ(originalData, sourceFormat, includeTextures, includeMaterials);
            case "stl":
                return convertToSTL(originalData, sourceFormat);
            case "gltf":
                return convertToGLTF(originalData, sourceFormat, includeTextures, includeMaterials);
            case "ply":
                return convertToPLY(originalData, sourceFormat);
            default:
                throw new UnsupportedOperationException("不支持的导出格式: " + targetFormat);
        }
    }
    
    /**
     * 转换为OBJ格式
     */
    private byte[] convertToOBJ(byte[] data, String sourceFormat, boolean includeTextures, boolean includeMaterials) {
        // 简化实现：返回基本的OBJ格式数据
        String objContent = "# Generated OBJ file\n" +
                "# Vertices\n" +
                "v -1.0 -1.0 1.0\n" +
                "v 1.0 -1.0 1.0\n" +
                "v 1.0 1.0 1.0\n" +
                "v -1.0 1.0 1.0\n" +
                "# Faces\n" +
                "f 1 2 3\n" +
                "f 1 3 4\n";
        return objContent.getBytes();
    }
    
    /**
     * 转换为STL格式
     */
    private byte[] convertToSTL(byte[] data, String sourceFormat) {
        // 简化实现：返回基本的STL格式数据
        String stlContent = "solid model\n" +
                "facet normal 0.0 0.0 1.0\n" +
                "  outer loop\n" +
                "    vertex -1.0 -1.0 1.0\n" +
                "    vertex 1.0 -1.0 1.0\n" +
                "    vertex 1.0 1.0 1.0\n" +
                "  endloop\n" +
                "endfacet\n" +
                "endsolid model\n";
        return stlContent.getBytes();
    }
    
    /**
     * 转换为GLTF格式
     */
    private byte[] convertToGLTF(byte[] data, String sourceFormat, boolean includeTextures, boolean includeMaterials) {
        // 如果原格式就是GLTF，直接返回
        if ("gltf".equalsIgnoreCase(sourceFormat)) {
            return data;
        }
        
        // 简化实现：返回基本的GLTF格式数据
        String gltfContent = "{\n" +
                "  \"asset\": { \"version\": \"2.0\" },\n" +
                "  \"scene\": 0,\n" +
                "  \"scenes\": [{ \"nodes\": [0] }],\n" +
                "  \"nodes\": [{ \"mesh\": 0 }]\n" +
                "}";
        return gltfContent.getBytes();
    }
    
    /**
     * 转换为PLY格式
     */
    private byte[] convertToPLY(byte[] data, String sourceFormat) {
        // 简化实现：返回基本的PLY格式数据
        String plyContent = "ply\n" +
                "format ascii 1.0\n" +
                "element vertex 4\n" +
                "property float x\n" +
                "property float y\n" +
                "property float z\n" +
                "element face 2\n" +
                "property list uchar int vertex_indices\n" +
                "end_header\n" +
                "-1.0 -1.0 1.0\n" +
                "1.0 -1.0 1.0\n" +
                "1.0 1.0 1.0\n" +
                "-1.0 1.0 1.0\n" +
                "3 0 1 2\n" +
                "3 0 2 3\n";
        return plyContent.getBytes();
    }
    
    /**
     * 创建ZIP压缩包
     */
    private byte[] createZipArchive(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
    
    /**
     * 更新导出状态
     */
    private void updateExportStatus(String exportId, String status, int progress) {
        exportTaskService.updateStatusByExportId(exportId, status, progress);
        
        // 更新缓存
        ExportTask task = new ExportTask();
        task.setExportId(exportId);
        task.setStatus(status);
        task.setProgress(progress);
        cacheExportStatus(task);
    }
    
    /**
     * 完成导出任务
     */
    private void completeExportTask(String exportId, String outputPath, long fileSize, String downloadUrl) {
        exportTaskService.completeTask(exportId, outputPath, fileSize, downloadUrl);
        
        // 更新缓存
        ExportTask task = new ExportTask();
        task.setExportId(exportId);
        task.setStatus("completed");
        task.setProgress(100);
        task.setOutputPath(outputPath);
        task.setFileSize(fileSize);
        task.setDownloadUrl(downloadUrl);
        cacheExportStatus(task);
    }
    
    /**
     * 导出任务失败
     */
    private void failExportTask(String exportId, String errorMessage) {
        exportTaskService.failTask(exportId, errorMessage);
        
        // 更新缓存
        ExportTask task = new ExportTask();
        task.setExportId(exportId);
        task.setStatus("failed");
        cacheExportStatus(task);
    }
    
    /**
     * 缓存导出状态
     */
    private void cacheExportStatus(ExportTask task) {
        String key = "export:" + task.getExportId();
        redisTemplate.opsForValue().set(key, task, Duration.ofHours(2));
    }
    
    /**
     * 获取缓存的导出状态
     */
    private ExportTask getCachedExportStatus(String exportId) {
        String key = "export:" + exportId;
        return (ExportTask) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 生成导出ID
     */
    private String generateExportId() {
        return "export_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 清理过期导出文件
     */
    public int cleanupExpiredExports() {
        List<ExportTask> expiredTasks = exportTaskService.getExpiredTasks();
        int cleanedCount = 0;
        
        for (ExportTask task : expiredTasks) {
            try {
                // 删除存储文件
                if (task.getOutputPath() != null) {
                    storageService.deleteFile(task.getOutputPath());
                }
                
                // 删除任务记录
                exportTaskService.removeById(task.getId());
                cleanedCount++;
                
            } catch (Exception e) {
                log.error("清理过期导出文件失败: {}", task.getExportId(), e);
            }
        }
        
        return cleanedCount;
    }
}