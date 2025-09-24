package com.generate3d.controller;

import com.generate3d.common.Result;
import com.generate3d.dto.CreateTextJobRequest;
import com.generate3d.service.GenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping("/text")
    public Result<String> createText(@RequestBody CreateTextJobRequest req) {
        String taskId = generationService.createTextJob(
                req.getPrompt(), Boolean.TRUE.equals(req.getPbr()), req.getOutFormat()
        );
        return Result.success("创建成功", taskId);
    }
}


