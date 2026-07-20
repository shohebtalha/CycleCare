package com.cyclecare.controller;

import com.cyclecare.dto.RegistrationDto;
import com.cyclecare.repository.UserRepository;
import com.cyclecare.service.MailService;
import com.cyclecare.service.ReportRange;
import com.cyclecare.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportExportTest {

    private static final String EMAIL = "report-export@example.com";
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
        dto.setName("Report Tester");
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
    void exportsPdfForEverySupportedReportRange() throws Exception {
        for (ReportRange range : ReportRange.values()) {
            mockMvc.perform(get("/reports/export")
                            .param("range", range.getRequestValue())
                            .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/pdf"))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            containsString("cyclecare-health-report-" + range.getRequestValue() + ".pdf")))
                    .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsByteArray())
                            .startsWith("%PDF".getBytes()));
        }
    }

    @Test
    void defaultsUnknownRangeToPastMonthPdf() throws Exception {
        mockMvc.perform(get("/reports/export")
                        .param("range", "not-a-range")
                        .with(SecurityMockMvcRequestPostProcessors.user(EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("cyclecare-health-report-past_month.pdf")))
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsByteArray().length)
                        .isGreaterThan(1000));
    }
}
