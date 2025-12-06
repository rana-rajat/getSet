package com.getset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class GetSetApplication {
    public static void main(String[] args) {
        SpringApplication.run(GetSetApplication.class, args);
    }
}
