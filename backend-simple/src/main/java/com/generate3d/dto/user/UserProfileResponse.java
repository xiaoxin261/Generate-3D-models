package com.generate3d.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
@Schema(description = "用户信息响应")
public class UserProfileResponse {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;
    
    @Schema(description = "生日")
    private LocalDate birthday;
    
    @Schema(description = "地区")
    private String region;
    
    @Schema(description = "个人简介")
    private String bio;
    
    @Schema(description = "邮箱是否验证")
    private Boolean emailVerified;
    
    @Schema(description = "手机是否验证")
    private Boolean phoneVerified;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}