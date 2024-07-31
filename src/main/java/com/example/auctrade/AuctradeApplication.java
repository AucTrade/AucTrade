package com.example.auctrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableMongoAuditing
@EnableScheduling
@SpringBootApplication
public class AuctradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctradeApplication.class, args);
    }

}
