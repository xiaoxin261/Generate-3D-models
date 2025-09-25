package com.generate3d.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobProgressResponse {
    private String taskId;
    private String status;
    private Integer progress;
    private Map<String, String> resultUrls;
    private String errorMsg;
}


