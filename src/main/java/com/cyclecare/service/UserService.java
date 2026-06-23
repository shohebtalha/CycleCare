package com.cyclecare.service;

import com.cyclecare.domain.ActivityLevel;
import com.cyclecare.domain.User;
import com.cyclecare.dto.ProfileDto;
import com.cyclecare.dto.RegistrationDto;
import com.cyclecare.dto.ResetPasswordDto;
import com.cyclecare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationDto dto) {
        String email = normalizeEmail(dto.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setActivityLevel(ActivityLevel.MODERATE);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user could not be found."));
    }

    @Transactional(readOnly = true)
    public ProfileDto toProfileDto(User user) {
        ProfileDto dto = new ProfileDto();
        dto.setName(user.getName());
        dto.setAge(user.getAge());
        dto.setHeight(user.getHeight());
        dto.setWeight(user.getWeight());
        dto.setActivityLevel(user.getActivityLevel());
        return dto;
    }

    @Transactional
    public User updateProfile(User user, ProfileDto dto) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        managedUser.setName(dto.getName().trim());
        managedUser.setAge(dto.getAge());
        managedUser.setHeight(dto.getHeight());
        managedUser.setWeight(dto.getWeight());
        managedUser.setActivityLevel(dto.getActivityLevel());
        return userRepository.save(managedUser);
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .map(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setResetToken(token);
                    user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
                    userRepository.save(user);
                    return token;
                })
                .orElse(null);
    }

    @Transactional
    public void resetPassword(ResetPasswordDto dto) {
        User user = userRepository.findByResetToken(dto.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired password reset token.");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
