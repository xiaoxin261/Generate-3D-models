package com.generate3d.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("login_logs")
public class LoginLog {
    
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
     * 登录用户名
     */
    private String username;
    
    /**
     * 登录类型：1-密码，2-验证码，3-第三方
     */
    private Integer loginType;
    
    /**
     * 登录状态：0-失败，1-成功
     */
    private Integer loginStatus;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
    
    /**
     * 登录地点
     */
    private String location;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}