package com.generate3d.controller;

import com.generate3d.dto.user.ChangePasswordRequest;
import com.generate3d.dto.user.UpdateProfileRequest;
import com.generate3d.dto.user.UserProfileResponse;
import com.generate3d.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    @Operation(summary = "获取用户资料", description = "获取当前登录用户的详细资料",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = true)
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            UserProfileResponse profile = userService.getUserProfile(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户资料失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/profile")
    @Operation(summary = "更新用户资料", description = "更新当前登录用户的资料信息",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = true)
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                           Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userService.updateProfile(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "资料更新成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新用户资料失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = true)
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userService.changePassword(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "密码修改成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/info")
    @Operation(summary = "获取用户基本信息", description = "获取当前登录用户的基本信息（如果已登录）",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌（可选）", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = false)
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 检查是否已认证
            if (authentication == null || authentication.getPrincipal() == null) {
                // 未认证用户返回基本信息
                response.put("success", true);
                response.put("authenticated", false);
                response.put("message", "用户未登录");
                return ResponseEntity.ok(response);
            }
            
            // 已认证用户返回详细信息
            Long userId = (Long) authentication.getPrincipal();
            UserProfileResponse profile = userService.getUserProfile(userId);
            
            // 只返回基本信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", profile.getId());
            userInfo.put("username", profile.getUsername());
            userInfo.put("nickname", profile.getNickname());
            userInfo.put("avatar", profile.getAvatar());
            userInfo.put("email", profile.getEmail());
            userInfo.put("phone", profile.getPhone());
            
            response.put("success", true);
            response.put("authenticated", true);
            response.put("data", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}