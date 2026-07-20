package com.cyclecare.service;

import com.cyclecare.domain.PasswordResetToken;
import com.cyclecare.domain.User;
import com.cyclecare.dto.ResetPasswordDto;
import com.cyclecare.repository.PasswordResetTokenRepository;
import com.cyclecare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final MailService mailService = mock(MailService.class);
    private final PasswordResetService passwordResetService = new PasswordResetService(
            userRepository,
            tokenRepository,
            passwordEncoder,
            mailService,
            "https://cyclecare.example.com"
    );

    @Test
    void requestPasswordResetStoresOnlyDigestAndEmailsRawTokenUrl() {
        User user = new User();
        user.setEmail("person@example.com");
        user.setName("Person");
        when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
        AtomicReference<PasswordResetToken> savedToken = new AtomicReference<>();
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> {
            savedToken.set(invocation.getArgument(0));
            return invocation.getArgument(0);
        });

        passwordResetService.requestPasswordReset(" Person@Example.com ");

        assertThat(savedToken.get()).isNotNull();
        assertThat(savedToken.get().getToken()).hasSize(64);
        assertThat(savedToken.get().getExpiryTime()).isAfter(LocalDateTime.now());
        verify(tokenRepository).deleteByUserAndUsedFalse(user);
        verify(mailService).sendPasswordResetEmail(argThat(sentUser -> sentUser == user),
                argThat(url -> url.startsWith("https://cyclecare.example.com/reset-password?token=")
                        && !url.contains(savedToken.get().getToken())));
    }

    @Test
    void requestPasswordResetDoesNotRevealMissingEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset("missing@example.com");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPasswordUpdatesPasswordAndMarksTokenUsed() {
        User user = new User();
        user.setEmailVerified(false);
        user.setEnabled(false);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByToken(argThat(value -> value != null && value.length() == 64)))
                .thenReturn(Optional.of(token));

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken("raw-reset-token");
        dto.setPassword("New-password1");
        dto.setConfirmPassword("New-password1");

        passwordResetService.resetPassword(dto);

        assertThat(passwordEncoder.matches("New-password1", user.getPasswordHash())).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.isEnabled()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPasswordRejectsUsedToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(true);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByToken(any())).thenReturn(Optional.of(token));

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken("raw-reset-token");
        dto.setPassword("New-password1");

        assertThatThrownBy(() -> passwordResetService.resetPassword(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }
}
