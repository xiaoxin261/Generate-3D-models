package com.generate3d.dto;

import lombok.Data;

@Data
public class CreateTextJobRequest {
    private String prompt;
    private Boolean pbr;
    private String outFormat; // GLTF | OBJ
}


