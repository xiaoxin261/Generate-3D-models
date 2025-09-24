package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.Model;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 模型数据访问接口
 */
@Mapper
public interface ModelMapper extends BaseMapper<Model> {
    
    /**
     * 根据模型ID查询模型
     */
    @Select("SELECT * FROM t_model WHERE model_id = #{modelId} AND status = 1")
    Model selectByModelId(@Param("modelId") String modelId);
    
    /**
     * 更新模型收藏状态
     */
    @Update("UPDATE t_model SET favorite = #{favorite}, updated_at = NOW() WHERE model_id = #{modelId}")
    int updateFavoriteByModelId(@Param("modelId") String modelId, @Param("favorite") Integer favorite);
    
    /**
     * 软删除模型
     */
    @Update("UPDATE t_model SET status = 0, updated_at = NOW() WHERE model_id = #{modelId}")
    int deleteByModelId(@Param("modelId") String modelId);
    
    /**
     * 根据分类查询模型列表
     */
    @Select("SELECT * FROM t_model WHERE category = #{category} AND status = 1 ORDER BY created_at DESC")
    List<Model> selectByCategory(@Param("category") String category);
    
    /**
     * 查询收藏的模型列表
     */
    @Select("SELECT * FROM t_model WHERE favorite = 1 AND status = 1 ORDER BY created_at DESC")
    List<Model> selectFavoriteModels();
    
    /**
     * 统计模型数量
     */
    @Select("SELECT COUNT(*) FROM t_model WHERE status = 1")
    Long countActiveModels();
    
    /**
     * 根据关键词搜索模型
     */
    @Select("SELECT * FROM t_model WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 ORDER BY created_at DESC")
    List<Model> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 获取分类统计
     */
    @Select("SELECT category, COUNT(*) as count FROM t_model WHERE status = 1 GROUP BY category")
    List<java.util.Map<String, Object>> getCategoryStatistics();
}