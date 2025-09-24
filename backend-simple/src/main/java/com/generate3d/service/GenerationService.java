package com.generate3d.service;

import com.generate3d.repo.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private final StorageService storageService;
    private final JobRepository jobRepository = new JobRepository();

    public String createTextJob(String prompt, boolean pbr, String outFormat) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        JobRepository.JobRecord r = new JobRepository.JobRecord();
        r.taskId = taskId;
        r.mode = "TEXT";
        r.prompt = prompt;
        r.status = "PENDING";
        r.progress = 0;
        jobRepository.save(r);
        runMockAsync(taskId);
        return taskId;
    }

    public JobRepository.JobRecord getJob(String taskId) {
        return jobRepository.find(taskId);
    }

    @Async
    protected void runMockAsync(String taskId) {
        JobRepository.JobRecord r = jobRepository.find(taskId);
        if (r == null) return;
        r.status = "RUNNING";
        try {
            for (int i = 1; i <= 10; i++) {
                Thread.sleep(300);
                r.progress = i * 10;
            }
            // 产出占位 glb 内容
            byte[] glbData = ("// mock glb for task " + taskId).getBytes(StandardCharsets.UTF_8);
            String glbKey = taskId + "/source/model.glb";
            storageService.putObject(glbKey, new java.io.ByteArrayInputStream(glbData), "model/gltf-binary");
            String glbUrl = storageService.generateSignedUrl(glbKey);
            r.urls.put("gltf", glbUrl);
            // 占位缩略图（用文本代替）
            byte[] png = ("PNG MOCK " + taskId).getBytes(StandardCharsets.UTF_8);
            String thumbKey = taskId + "/preview/thumb.png";
            storageService.putObject(thumbKey, new java.io.ByteArrayInputStream(png), "image/png");
            String thumbUrl = storageService.generateSignedUrl(thumbKey);
            r.urls.put("thumbnail", thumbUrl);
            r.status = "SUCCEEDED";
            r.finishedAt = System.currentTimeMillis();
        } catch (Exception e) {
            r.status = "FAILED";
            r.errorMsg = e.getMessage();
        }
    }
}


