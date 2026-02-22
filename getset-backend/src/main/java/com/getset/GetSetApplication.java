package com.getset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class GetSetApplication {
    public static void main(String[] args) {
        SpringApplication.run(GetSetApplication.class, args);
    }
}
