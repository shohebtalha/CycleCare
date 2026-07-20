package com.cyclecare.controller;

import com.cyclecare.dto.RegistrationDto;
import com.cyclecare.repository.UserRepository;
import com.cyclecare.service.MailService;
import com.cyclecare.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFlowTest {

    private static final String EMAIL = "security-flow@example.com";
    private static final String PASSWORD = "strong-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private MailService mailService;

    @BeforeEach
    void createUser() {
        RegistrationDto dto = new RegistrationDto();
        dto.setName("Security Tester");
        dto.setEmail(EMAIL);
        dto.setPassword(PASSWORD);
        dto.setConfirmPassword(PASSWORD);
        dto.setAcceptedPrivacyPolicy(true);
        dto.setAcceptedTerms(true);
        try {
            userService.register(dto);
        } catch (IllegalArgumentException ignored) {
            // Test context may be reused between methods.
        }
        userRepository.findByEmailIgnoreCase(EMAIL).ifPresent(user -> {
            user.setEmailVerified(true);
            user.setEnabled(true);
            userRepository.save(user);
        });
    }

    @Test
    void redirectsAnonymousUsersAwayFromPrivatePages() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void rejectsStateChangingRequestsWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/water")
                        .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER"))
                        .with(csrf().useInvalidToken())
                        .param("entryDate", "2026-07-06")
                        .param("amountMl", "250"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsLoginAndAccessToAuthenticatedPage() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", EMAIL)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        mockMvc.perform(get("/dashboard")
                        .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CycleCare")));
    }

    @Test
    void logoutRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER"))
                        .with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/logout")
                        .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void rateLimitsAssistantRequests() throws Exception {
        for (int index = 0; index < 3; index++) {
            mockMvc.perform(post("/api/assistant")
                            .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER"))
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"question\":\"What should I eat today?\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/assistant")
                        .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"question\":\"One more question?\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
