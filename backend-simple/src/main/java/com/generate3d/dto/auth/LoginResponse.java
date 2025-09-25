package com.generate3d.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户登录响应DTO
 */
@Data
@Schema(description = "用户登录响应")
public class LoginResponse {
    
    @Schema(description = "访问令牌")
    private String token;
    
    @Schema(description = "刷新令牌")
    private String refreshToken;
    
    @Schema(description = "用户信息")
    private UserInfo user;
    
    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID")
        private Long id;
        
        @Schema(description = "用户名")
        private String username;
        
        @Schema(description = "昵称")
        private String nickname;
        
        @Schema(description = "头像")
        private String avatar;
        
        @Schema(description = "角色列表")
        private List<String> roles;
    }
}