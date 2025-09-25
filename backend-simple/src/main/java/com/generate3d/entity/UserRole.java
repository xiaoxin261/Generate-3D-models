package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_roles")
public class UserRole {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色代码
     */
    private String roleCode;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 授权时间
     */
    private LocalDateTime grantedAt;
    
    /**
     * 授权人ID
     */
    private Long grantedBy;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}