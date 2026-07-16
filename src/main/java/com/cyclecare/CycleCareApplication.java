package com.cyclecare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "CycleCare API", version = "1.0", description = "REST APIs for CycleCare Menstrual Health Platform"))
public class CycleCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CycleCareApplication.class, args);
    }
}
