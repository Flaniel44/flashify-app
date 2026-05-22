package com.spagnuolo.flashify_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlashifyAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlashifyAppApplication.class, args);
    }
}