package com.agrocloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgroCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroCloudApplication.class, args);
    }
}
