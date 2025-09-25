package com.generate3d.service;

import com.generate3d.dto.auth.LoginRequest;
import com.generate3d.dto.auth.LoginResponse;
import com.generate3d.dto.auth.RegisterRequest;
import com.generate3d.dto.user.ChangePasswordRequest;
import com.generate3d.dto.user.UpdateProfileRequest;
import com.generate3d.dto.user.UserProfileResponse;
import com.generate3d.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    void register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    
    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 获取用户资料
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 更新用户资料
     */
    void updateProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
}