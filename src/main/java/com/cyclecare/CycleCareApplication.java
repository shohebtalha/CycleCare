package com.cyclecare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CycleCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CycleCareApplication.class, args);
    }
}
