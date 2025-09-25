package com.generate3d.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generate3d.entity.Model;
import com.generate3d.mapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 模型服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelService extends ServiceImpl<ModelMapper, Model> {
    
    private final StorageService storageService;
    
    /**
     * 根据模型ID查询模型
     */
    public Model getByModelId(String modelId) {
        return baseMapper.selectByModelId(modelId);
    }
    
    /**
     * 获取模型列表（分页）
     */
    public Page<Model> getModelList(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Model::getStatus, 1)
                .like(StringUtils.hasText(keyword), Model::getName, keyword)
                .or(StringUtils.hasText(keyword), w -> w.like(Model::getDescription, keyword))
                .eq(StringUtils.hasText(category), Model::getCategory, category)
                .orderByDesc(Model::getCreatedAt);
        
        Page<Model> pageParam = new Page<>(page, size);
        return this.page(pageParam, wrapper);
    }
    
    /**
     * 获取收藏的模型列表
     */
    public List<Model> getFavoriteModels() {
        return baseMapper.selectFavoriteModels();
    }
    
    /**
     * 获取收藏的模型列表（分页）
     */
    public Page<Model> getFavoriteModels(int page, int size) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Model::getStatus, 1)
                .eq(Model::getFavorite, 1)
                .orderByDesc(Model::getCreatedAt);
        
        Page<Model> pageParam = new Page<>(page, size);
        return this.page(pageParam, wrapper);
    }
    
    /**
     * 根据分类查询模型
     */
    public List<Model> getModelsByCategory(String category) {
        return baseMapper.selectByCategory(category);
    }
    
    /**
     * 搜索模型
     */
    public List<Model> searchModels(String keyword) {
        return baseMapper.searchByKeyword(keyword);
    }
    
    /**
     * 保存模型信息
     */
    public Model saveModel(Model model) {
        if (!StringUtils.hasText(model.getModelId())) {
            model.setModelId(generateModelId());
        }
        if (model.getStatus() == null) {
            model.setStatus(1);
        }
        if (model.getFavorite() == null) {
            model.setFavorite(0);
        }
        if (!StringUtils.hasText(model.getFileFormat())) {
            model.setFileFormat("gltf");
        }
        
        this.save(model);
        return model;
    }
    
    /**
     * 更新模型信息
     */
    public boolean updateModel(Model model) {
        return this.updateById(model);
    }
    
    /**
     * 切换收藏状态
     */
    public boolean toggleFavorite(String modelId) {
        Model model = getByModelId(modelId);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        
        int newFavorite = model.getFavorite() == 1 ? 0 : 1;
        return baseMapper.updateFavoriteByModelId(modelId, newFavorite) > 0;
    }
    
    /**
     * 设置收藏状态
     */
    public boolean toggleFavorite(String modelId, boolean favorite) {
        Model model = getByModelId(modelId);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        
        int favoriteValue = favorite ? 1 : 0;
        return baseMapper.updateFavoriteByModelId(modelId, favoriteValue) > 0;
    }
    
    /**
     * 删除模型（软删除）
     */
    public boolean deleteModel(String modelId) {
        Model model = getByModelId(modelId);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        
        // 软删除
        boolean result = baseMapper.deleteByModelId(modelId) > 0;
        
        // 异步删除文件
        if (result) {
            deleteModelFilesAsync(model);
        }
        
        return result;
    }
    
    /**
     * 异步删除模型文件
     */
    @Async
    public void deleteModelFilesAsync(Model model) {
        try {
            if (StringUtils.hasText(model.getFilePath())) {
                storageService.deleteFile(model.getFilePath());
            }
            if (StringUtils.hasText(model.getThumbnailPath())) {
                storageService.deleteFile(model.getThumbnailPath());
            }
            log.info("模型文件删除成功: {}", model.getModelId());
        } catch (Exception e) {
            log.error("删除模型文件失败: {}", model.getModelId(), e);
        }
    }
    
    /**
     * 获取模型统计信息
     */
    public Long getModelCount() {
        return baseMapper.countActiveModels();
    }
    
    /**
     * 获取模型统计信息（详细）
     */
    public java.util.Map<String, Object> getModelStatistics() {
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalModels", getModelCount());
        statistics.put("favoriteModels", getFavoriteModels().size());
        statistics.put("categories", baseMapper.getCategoryStatistics());
        return statistics;
    }
    
    /**
     * 根据模型ID列表获取模型
     */
    public List<Model> getModelsByIds(List<String> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return baseMapper.selectList(
            new LambdaQueryWrapper<Model>()
                .in(Model::getModelId, modelIds)
                .eq(Model::getStatus, 1)
        );
    }
    
    /**
     * 生成模型ID
     */
    private String generateModelId() {
        return "model_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 根据任务ID更新模型信息
     */
    public boolean updateModelByTaskId(String taskId, String modelId, String filePath, String thumbnailPath, 
                                      Integer verticesCount, Integer facesCount, Long fileSize) {
        Model model = getByModelId(modelId);
        if (model == null) {
            return false;
        }
        
        model.setFilePath(filePath);
        model.setThumbnailPath(thumbnailPath);
        model.setVerticesCount(verticesCount);
        model.setFacesCount(facesCount);
        model.setFileSize(fileSize);
        model.setUpdatedAt(LocalDateTime.now());
        
        return this.updateById(model);
    }
}