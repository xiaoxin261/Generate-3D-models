package com.generate3d.controller;

import com.generate3d.common.Result;
import com.generate3d.dto.CreateTextJobRequest;
import com.generate3d.service.GenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "任务生成", description = "3D模型生成任务相关接口")
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping("/text")
    @Operation(summary = "创建文本生成任务", description = "根据文本提示创建3D模型生成任务",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = false)
    public Result<String> createText(@RequestBody CreateTextJobRequest req) {
        String taskId = generationService.createTextJob(
                req.getPrompt(), Boolean.TRUE.equals(req.getPbr()), req.getOutFormat()
        );
        return Result.success("创建成功", taskId);
    }
}


