package com.generate3d.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {
    
    @NotBlank(message = "登录名不能为空")
    @Schema(description = "登录名（用户名/邮箱/手机号）", example = "testuser")
    private String loginName;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
    
    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;
    
    @Schema(description = "验证码", example = "1234")
    private String captcha;
}