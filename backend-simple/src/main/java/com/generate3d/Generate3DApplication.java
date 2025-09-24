package com.generate3d;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 3D模型生成系统主启动类
 * 
 * @author Generate3D Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class Generate3DApplication {

    public static void main(String[] args) {
        SpringApplication.run(Generate3DApplication.class, args);
        System.out.println("\n" +
                "  ____                           _       _____ ____  \n" +
                " / ___| ___ _ __   ___ _ __ __ _| |_ ___|___ /|  _ \\ \n" +
                " | |  _ / _ \\ '_ \\ / _ \\ '__/ _` | __/ _ \\ |_ \\| | | |\n" +
                " | |_| |  __/ | | |  __/ | | (_| | ||  __/___) | |_| |\n" +
                "  \\____|___|_| |_|\\___|_|  \\__,_|\\__\\___|____/|____/ \n" +
                "\n" +
                "Generate 3D Backend Service Started Successfully!\n" +
                "API Documentation: http://localhost:8080/swagger-ui.html\n" +
                "Health Check: http://localhost:8080/actuator/health\n");
    }
}