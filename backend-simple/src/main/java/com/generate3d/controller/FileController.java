package com.generate3d.controller;

import com.generate3d.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "oss")
public class FileController {

    private final OssService ossService;

    public FileController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到OSS存储")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件夹路径，默认为'temp'") @RequestParam(value = "folder", defaultValue = "temp") String folder) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不能为空"));
            }

            // 检查文件大小（100MB限制）
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件大小不能超过100MB"));
            }

            String fileUrl = ossService.uploadFile(file, folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("data", Map.of(
                    "url", fileUrl,
                    "originalName", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "contentType", file.getContentType()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("文件上传失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据文件URL删除OSS中的文件")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @Parameter(description = "文件URL") @RequestParam("url") String url) {
        
        try {
            String objectKey = ossService.extractObjectKeyFromUrl(url);
            if (objectKey == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件URL"));
            }

            if (!ossService.doesObjectExist(objectKey)) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不存在"));
            }

            ossService.deleteFile(objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("文件删除失败: " + e.getMessage()));
        }
    }

    @GetMapping("/info")
    @Operation(summary = "获取文件信息", description = "根据文件URL获取文件的详细信息")
    public ResponseEntity<Map<String, Object>> getFileInfo(
            @Parameter(description = "文件URL") @RequestParam("url") String url) {
        
        try {
            String objectKey = ossService.extractObjectKeyFromUrl(url);
            if (objectKey == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的文件URL"));
            }

            if (!ossService.doesObjectExist(objectKey)) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不存在"));
            }

            var metadata = ossService.getObjectMetadata(objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取文件信息成功");
            response.put("data", Map.of(
                    "objectKey", objectKey,
                    "size", metadata.getContentLength(),
                    "contentType", metadata.getContentType(),
                    "lastModified", metadata.getLastModified(),
                    "eTag", metadata.getETag()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取文件信息失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}