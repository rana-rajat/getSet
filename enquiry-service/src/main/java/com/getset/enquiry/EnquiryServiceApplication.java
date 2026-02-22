package com.getset.enquiry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
@EnableFeignClients
public class EnquiryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnquiryServiceApplication.class, args);
    }
}
