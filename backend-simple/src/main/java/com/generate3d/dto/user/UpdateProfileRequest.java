package com.generate3d.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新用户信息请求DTO
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateProfileRequest {
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "测试用户")
    private String nickname;
    
    @Schema(description = "性别：0-未知，1-男，2-女", example = "1")
    private Integer gender;
    
    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;
    
    @Size(max = 100, message = "地区长度不能超过100个字符")
    @Schema(description = "地区", example = "北京市")
    private String region;
    
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    @Schema(description = "个人简介", example = "这是个人简介")
    private String bio;
}