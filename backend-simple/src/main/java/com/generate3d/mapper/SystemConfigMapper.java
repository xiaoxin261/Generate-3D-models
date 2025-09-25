package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 系统配置数据访问接口
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM t_system_config WHERE config_key = #{configKey}")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 更新配置值
     */
    @Update("UPDATE t_system_config SET config_value = #{configValue}, updated_at = NOW() WHERE config_key = #{configKey}")
    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
    
    /**
     * 根据配置类型查询配置列表
     */
    @Select("SELECT * FROM t_system_config WHERE config_type = #{configType} ORDER BY config_key")
    List<SystemConfig> selectByConfigType(@Param("configType") String configType);
    
    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM t_system_config ORDER BY config_key")
    List<SystemConfig> selectAllConfigs();
}