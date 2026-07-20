package com.cyclecare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ConsentGuardInterceptor consentGuardInterceptor;

    public WebMvcConfig(ConsentGuardInterceptor consentGuardInterceptor) {
        this.consentGuardInterceptor = consentGuardInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(consentGuardInterceptor);
    }
}
