package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色数据访问层
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID查找用户角色
     */
    @Select("SELECT * FROM user_roles WHERE user_id = #{userId} AND (expires_at IS NULL OR expires_at > NOW())")
    List<UserRole> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和角色代码查找角色
     */
    @Select("SELECT * FROM user_roles WHERE user_id = #{userId} AND role_code = #{roleCode} AND (expires_at IS NULL OR expires_at > NOW())")
    UserRole findByUserIdAndRoleCode(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}