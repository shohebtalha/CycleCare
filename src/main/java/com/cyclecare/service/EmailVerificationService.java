package com.cyclecare.service;

import com.cyclecare.domain.EmailVerificationToken;
import com.cyclecare.domain.User;
import com.cyclecare.repository.EmailVerificationTokenRepository;
import com.cyclecare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRY_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String baseUrl;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                    UserRepository userRepository,
                                    MailService mailService,
                                    @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public void sendVerification(User user) {
        tokenRepository.deleteByUserAndUsedFalse(user);
        String rawToken = generateToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(hashToken(rawToken));
        token.setExpiryTime(LocalDateTime.now().plusHours(EXPIRY_HOURS));
        tokenRepository.save(token);
        mailService.sendEmailVerificationEmail(user, buildVerificationUrl(rawToken));
        logger.info("Email verification queued for user id {}", user.getId());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        EmailVerificationToken token = findUsableToken(rawToken);
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This verification link has expired. Please register again or request a new link.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        token.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(token);
    }

    private EmailVerificationToken findUsableToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Verification token is missing.");
        }
        return tokenRepository.findByToken(hashToken(rawToken))
                .filter(token -> !token.isUsed())
                .orElseThrow(() -> new IllegalArgumentException("This verification link is invalid or has already been used."));
    }

    private String buildVerificationUrl(String token) {
        String safeBaseUrl = baseUrl == null || baseUrl.isBlank() ? "http://localhost:8080" : baseUrl.trim();
        return UriComponentsBuilder.fromHttpUrl(safeBaseUrl)
                .path("/verify-email")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
