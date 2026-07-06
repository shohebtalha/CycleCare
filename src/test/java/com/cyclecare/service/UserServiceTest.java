package com.cyclecare.service;

import com.cyclecare.domain.User;
import com.cyclecare.dto.ResetPasswordDto;
import com.cyclecare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService = new UserService(userRepository, passwordEncoder);

    @Test
    void createPasswordResetTokenStoresOnlyDigest() {
        User user = new User();
        when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));

        String rawToken = userService.createPasswordResetToken(" Person@Example.com ");

        assertThat(rawToken).isNotBlank();
        assertThat(user.getResetToken()).isNotEqualTo(rawToken);
        assertThat(user.getResetToken()).hasSize(64);
        assertThat(user.getResetTokenExpiresAt()).isAfter(LocalDateTime.now());
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordLooksUpDigestAndClearsResetToken() {
        User user = new User();
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByResetToken(argThat(token -> token != null && token.length() == 64)))
                .thenReturn(Optional.of(user));

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken("raw-reset-token");
        dto.setPassword("New-password1");
        dto.setConfirmPassword("New-password1");

        userService.resetPassword(dto);

        assertThat(passwordEncoder.matches("New-password1", user.getPasswordHash())).isTrue();
        assertThat(user.getResetToken()).isNull();
        assertThat(user.getResetTokenExpiresAt()).isNull();
        verify(userRepository).save(user);
    }
}
