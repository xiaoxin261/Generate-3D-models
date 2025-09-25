package com.generate3d.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generate3d.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND status != 0")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND status != 0")
    User findByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查找用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND status != 0")
    User findByPhone(@Param("phone") String phone);
    
    /**
     * 根据用户名、邮箱或手机号查找用户（用于登录）
     */
    @Select("SELECT * FROM users WHERE (username = #{loginName} OR email = #{loginName} OR phone = #{loginName}) AND status != 0")
    User findByLoginName(@Param("loginName") String loginName);
}