package com.cyclecare.service;

import com.cyclecare.domain.EmailVerificationToken;
import com.cyclecare.domain.User;
import com.cyclecare.repository.EmailVerificationTokenRepository;
import com.cyclecare.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    private final EmailVerificationTokenRepository tokenRepository = mock(EmailVerificationTokenRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MailService mailService = mock(MailService.class);
    private final EmailVerificationService service = new EmailVerificationService(
            tokenRepository,
            userRepository,
            mailService,
            "https://cyclecare.example.com"
    );

    @Test
    void sendVerificationStoresDigestAndEmailsRawTokenUrl() {
        User user = new User();
        user.setEmail("person@example.com");
        user.setName("Person");
        AtomicReference<EmailVerificationToken> savedToken = new AtomicReference<>();
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(invocation -> {
            savedToken.set(invocation.getArgument(0));
            return invocation.getArgument(0);
        });

        service.sendVerification(user);

        assertThat(savedToken.get()).isNotNull();
        assertThat(savedToken.get().getToken()).hasSize(64);
        assertThat(savedToken.get().getExpiryTime()).isAfter(LocalDateTime.now());
        verify(tokenRepository).deleteByUserAndUsedFalse(user);
        verify(mailService).sendEmailVerificationEmail(argThat(sentUser -> sentUser == user),
                argThat(url -> url.startsWith("https://cyclecare.example.com/verify-email?token=")
                        && !url.contains(savedToken.get().getToken())));
    }

    @Test
    void verifyEmailActivatesUserAndMarksTokenUsed() {
        User user = new User();
        user.setEnabled(false);
        user.setEmailVerified(false);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByToken(argThat(value -> value != null && value.length() == 64)))
                .thenReturn(Optional.of(token));

        service.verifyEmail("raw-verification-token");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmailIsIdempotentForAlreadyUsedToken() {
        User user = new User();
        user.setEnabled(false);
        user.setEmailVerified(false);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setUsed(true);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByToken(argThat(value -> value != null && value.length() == 64)))
                .thenReturn(Optional.of(token));

        service.verifyEmail("raw-verification-token");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmailRejectsExpiredToken() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmail("raw-verification-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
    }
}
