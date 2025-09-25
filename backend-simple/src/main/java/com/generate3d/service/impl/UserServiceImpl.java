package com.generate3d.service.impl;

import com.generate3d.dto.auth.LoginRequest;
import com.generate3d.dto.auth.LoginResponse;
import com.generate3d.dto.auth.RegisterRequest;
import com.generate3d.dto.user.ChangePasswordRequest;
import com.generate3d.dto.user.UpdateProfileRequest;
import com.generate3d.dto.user.UserProfileResponse;
import com.generate3d.entity.LoginLog;
import com.generate3d.entity.User;
import com.generate3d.entity.UserRole;
import com.generate3d.mapper.LoginLogMapper;
import com.generate3d.mapper.UserMapper;
import com.generate3d.mapper.UserRoleMapper;
import com.generate3d.security.JwtTokenProvider;
import com.generate3d.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查手机号是否已存在
        if (request.getPhone() != null && existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 检查密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername()); // 默认昵称为用户名
        user.setStatus(1); // 正常状态
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 分配默认角色
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setGrantedAt(LocalDateTime.now());
        userRole.setCreatedAt(LocalDateTime.now());
        
        userRoleMapper.insert(userRole);
        
        log.info("用户注册成功: {}", request.getUsername());
    }
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // 查找用户
        User user = userMapper.findByLoginName(request.getLoginName());
        if (user == null) {
            recordLoginLog(null, request.getLoginName(), 0, ipAddress, userAgent, "用户不存在");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginLog(user.getId(), request.getLoginName(), 0, ipAddress, userAgent, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            recordLoginLog(user.getId(), request.getLoginName(), 0, ipAddress, userAgent, "账户已禁用");
            throw new RuntimeException("账户已禁用");
        }
        
        // 获取用户角色
        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getId());
        List<String> roles = userRoles.stream()
                .map(UserRole::getRoleCode)
                .collect(Collectors.toList());
        
        // 生成令牌
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 记录登录日志
        recordLoginLog(user.getId(), request.getLoginName(), 1, ipAddress, userAgent, null);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setRoles(roles);
        response.setUser(userInfo);
        
        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }
        
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = getUserById(userId);
        
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已禁用");
        }
        
        // 获取用户角色
        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getId());
        List<String> roles = userRoles.stream()
                .map(UserRole::getRoleCode)
                .collect(Collectors.toList());
        
        // 生成新的令牌
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setRoles(roles);
        response.setUser(userInfo);
        
        return response;
    }
    
    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        UserProfileResponse response = new UserProfileResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
    
    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getRegion() != null) {
            user.setRegion(request.getRegion());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("用户资料更新成功: {}", user.getUsername());
    }
    
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 检查新密码确认
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("用户密码修改成功: {}", user.getUsername());
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userMapper.findByUsername(username) != null;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return email != null && userMapper.findByEmail(email) != null;
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        return phone != null && userMapper.findByPhone(phone) != null;
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, Integer loginStatus, 
                               String ipAddress, String userAgent, String failureReason) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginType(1); // 密码登录
        loginLog.setLoginStatus(loginStatus);
        loginLog.setIpAddress(ipAddress);
        loginLog.setUserAgent(userAgent);
        loginLog.setFailureReason(failureReason);
        loginLog.setCreatedAt(LocalDateTime.now());
        
        loginLogMapper.insert(loginLog);
    }
}