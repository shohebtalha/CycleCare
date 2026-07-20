package com.cyclecare.config;

import jakarta.annotation.PostConstruct;
import com.cyclecare.security.GoogleOAuth2UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String DEVELOPMENT_REMEMBER_ME_KEY = "dev-cyclecare-change-me";

    @Value("${cyclecare.security.remember-me-key}")
    private String rememberMeKey;

    private final Environment environment;
    private final ObjectProvider<GoogleOAuth2UserService> googleOAuth2UserService;

    public SecurityConfig(Environment environment, ObjectProvider<GoogleOAuth2UserService> googleOAuth2UserService) {
        this.environment = environment;
        this.googleOAuth2UserService = googleOAuth2UserService;
    }

    @PostConstruct
    void validateProductionSecrets() {
        boolean production = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (production && DEVELOPMENT_REMEMBER_ME_KEY.equals(rememberMeKey)) {
            throw new IllegalStateException("REMEMBER_ME_KEY must be set to a strong secret in production.");
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/forgot-password", "/reset-password",
                                "/verify-email", "/privacy", "/terms", "/consent",
                                "/oauth2/**", "/login/oauth2/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/error",
                                "/actuator/health", "/actuator/health/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .rememberMe(remember -> remember
                        .key(rememberMeKey)
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' https://cdn.jsdelivr.net https://unpkg.com 'unsafe-inline'; " +
                                        "style-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; " +
                                        "font-src 'self' https://cdn.jsdelivr.net; " +
                                        "img-src 'self' data:; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none'"))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login?expired=true")
                        .maximumSessions(1));

        if (isGoogleOAuthConfigured()) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?oauthError=true")
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(googleOAuth2UserService.getObject())));
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public boolean googleOAuthConfigured() {
        return isGoogleOAuthConfigured();
    }

    private boolean isGoogleOAuthConfigured() {
        return isConfiguredSecret("spring.security.oauth2.client.registration.google.client-id")
                && isConfiguredSecret("spring.security.oauth2.client.registration.google.client-secret");
    }

    private boolean isConfiguredSecret(String propertyName) {
        try {
            String value = environment.getProperty(propertyName);
            return hasText(value) && !value.startsWith("${") && !value.toLowerCase().contains("your_google");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
