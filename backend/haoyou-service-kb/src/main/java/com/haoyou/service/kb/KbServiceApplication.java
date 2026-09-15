package com.haoyou.service.kb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.haoyou")
public class KbServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KbServiceApplication.class, args);
    }
}
