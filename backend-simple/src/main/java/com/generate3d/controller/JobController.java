package com.generate3d.controller;

import com.generate3d.common.Result;
import com.generate3d.dto.JobProgressResponse;
import com.generate3d.repo.JobRepository;
import com.generate3d.service.GenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final GenerationService generationService;

    @GetMapping("/{taskId}")
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


