package com.haoyou.service.creator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.haoyou")
public class CreatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreatorServiceApplication.class, args);
    }
}
