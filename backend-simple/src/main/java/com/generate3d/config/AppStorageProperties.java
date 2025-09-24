package com.generate3d.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class AppStorageProperties {
    private String type = "oss"; // oss | local

    private final Oss oss = new Oss();
    private final Local local = new Local();

    @Data
    public static class Oss {
        private String endpoint;
        private String bucket;
        private String accessKeyId;
        private String accessKeySecret;
        private String baseDir = "models/";
        private int signedUrlExpireSeconds = 1800;
    }

    @Data
    public static class Local {
        private String rootDir = "./models";
    }
}


