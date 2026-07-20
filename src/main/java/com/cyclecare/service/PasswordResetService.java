package com.cyclecare.service;

import com.cyclecare.domain.PasswordResetToken;
import com.cyclecare.domain.User;
import com.cyclecare.dto.ResetPasswordDto;
import com.cyclecare.repository.PasswordResetTokenRepository;
import com.cyclecare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String baseUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                MailService mailService,
                                @Value("${app.base-url:}") String baseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresentOrElse(user -> {
            tokenRepository.deleteByUserAndUsedFalse(user);
            String rawToken = generateToken();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(hashToken(rawToken));
            resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
            tokenRepository.save(resetToken);
            mailService.sendPasswordResetEmail(user, buildResetUrl(rawToken));
            logger.info("Password reset email queued for user id {}", user.getId());
        }, () -> {
            logger.info("Password reset requested for non-existent email address");
        });
    }

    @Transactional(readOnly = true)
    public void validateResetToken(String rawToken) {
        PasswordResetToken token = findUsableToken(rawToken);
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This password reset link has expired. Please request a new one.");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordDto dto) {
        PasswordResetToken token = findUsableToken(dto.getToken());
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This password reset link has expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setEmailVerified(true);
        user.setEnabled(true);
        token.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(token);
    }

    private PasswordResetToken findUsableToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Password reset token is missing.");
        }
        return tokenRepository.findByToken(hashToken(rawToken))
                .filter(token -> !token.isUsed())
                .orElseThrow(() -> new IllegalArgumentException("This password reset link is invalid or has already been used."));
    }

    private String buildResetUrl(String token) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("APP_BASE_URL must be configured before sending password reset emails.");
        }
        return UriComponentsBuilder.fromHttpUrl(baseUrl.trim())
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", ex);
        }
    }
}
