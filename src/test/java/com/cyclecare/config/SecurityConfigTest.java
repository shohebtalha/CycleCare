package com.cyclecare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void rejectsDevelopmentRememberMeKeyInProduction() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        SecurityConfig securityConfig = new SecurityConfig(environment, mock(ObjectProvider.class));
        ReflectionTestUtils.setField(securityConfig, "rememberMeKey", "dev-cyclecare-change-me");

        assertThatThrownBy(securityConfig::validateProductionSecrets)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REMEMBER_ME_KEY");
    }

    @Test
    void allowsDevelopmentRememberMeKeyOutsideProduction() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"local"});
        SecurityConfig securityConfig = new SecurityConfig(environment, mock(ObjectProvider.class));
        ReflectionTestUtils.setField(securityConfig, "rememberMeKey", "dev-cyclecare-change-me");

        assertThatCode(securityConfig::validateProductionSecrets).doesNotThrowAnyException();
    }
}
