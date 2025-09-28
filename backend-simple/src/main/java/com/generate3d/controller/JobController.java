package com.generate3d.controller;

import com.generate3d.common.Result;
import com.generate3d.dto.JobProgressResponse;
import com.generate3d.repo.JobRepository;
import com.generate3d.service.GenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务进度查询相关接口")
public class JobController {

    private final GenerationService generationService;

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务进度", description = "根据任务ID获取任务执行进度",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Parameter(name = "Authorization", description = "JWT认证令牌", 
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER, required = false)
    public Result<JobProgressResponse> getJob(@PathVariable String taskId) {
        JobRepository.JobRecord r = generationService.getJob(taskId);
        if (r == null) return Result.error("任务不存在");
        JobProgressResponse resp = new JobProgressResponse(
                r.taskId, r.status, r.progress,
                new HashMap<>(r.urls), r.errorMsg
        );
        return Result.success("查询成功", resp);
    }
}


