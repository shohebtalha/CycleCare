package com.cyclecare.config;

import com.cyclecare.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cyclecare.security.remember-me-key}")
    private String rememberMeKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/forgot-password", "/reset-password",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/error").permitAll()
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
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login?expired=true")
                        .maximumSessions(1));

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
