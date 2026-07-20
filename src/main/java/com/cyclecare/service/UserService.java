package com.cyclecare.service;

import com.cyclecare.domain.ActivityLevel;
import com.cyclecare.domain.AuthProvider;
import com.cyclecare.domain.User;
import com.cyclecare.dto.ChangePasswordDto;
import com.cyclecare.dto.ProfileDto;
import com.cyclecare.dto.RegistrationDto;
import com.cyclecare.repository.CyclePredictionHistoryRepository;
import com.cyclecare.repository.CycleRepository;
import com.cyclecare.repository.EmailVerificationTokenRepository;
import com.cyclecare.repository.FlowRepository;
import com.cyclecare.repository.HealthInsightRepository;
import com.cyclecare.repository.JournalEntryRepository;
import com.cyclecare.repository.MoodRepository;
import com.cyclecare.repository.PartnerNotificationLogRepository;
import com.cyclecare.repository.PasswordResetTokenRepository;
import com.cyclecare.repository.SleepLogRepository;
import com.cyclecare.repository.SymptomRepository;
import com.cyclecare.repository.UserRepository;
import com.cyclecare.repository.UserConsentRepository;
import com.cyclecare.repository.WaterLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsentService consentService;
    private final EmailVerificationService emailVerificationService;
    private final CycleRepository cycleRepository;
    private final CyclePredictionHistoryRepository cyclePredictionHistoryRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final FlowRepository flowRepository;
    private final HealthInsightRepository healthInsightRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final MoodRepository moodRepository;
    private final PartnerNotificationLogRepository partnerNotificationLogRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SleepLogRepository sleepLogRepository;
    private final SymptomRepository symptomRepository;
    private final UserConsentRepository userConsentRepository;
    private final WaterLogRepository waterLogRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ConsentService consentService,
                       EmailVerificationService emailVerificationService,
                       CycleRepository cycleRepository,
                       CyclePredictionHistoryRepository cyclePredictionHistoryRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       FlowRepository flowRepository,
                       HealthInsightRepository healthInsightRepository,
                       JournalEntryRepository journalEntryRepository,
                       MoodRepository moodRepository,
                       PartnerNotificationLogRepository partnerNotificationLogRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       SleepLogRepository sleepLogRepository,
                       SymptomRepository symptomRepository,
                       UserConsentRepository userConsentRepository,
                       WaterLogRepository waterLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.consentService = consentService;
        this.emailVerificationService = emailVerificationService;
        this.cycleRepository = cycleRepository;
        this.cyclePredictionHistoryRepository = cyclePredictionHistoryRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.flowRepository = flowRepository;
        this.healthInsightRepository = healthInsightRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.moodRepository = moodRepository;
        this.partnerNotificationLogRepository = partnerNotificationLogRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.sleepLogRepository = sleepLogRepository;
        this.symptomRepository = symptomRepository;
        this.userConsentRepository = userConsentRepository;
        this.waterLogRepository = waterLogRepository;
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
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        user.setEnabled(false);
        user.setActivityLevel(ActivityLevel.MODERATE);
        User savedUser = userRepository.save(user);
        consentService.recordCurrentConsent(savedUser, toConsentAcceptance(dto));
        emailVerificationService.sendVerification(savedUser);
        return savedUser;
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
        dto.setPartnerEmail(user.getPartnerEmail());
        dto.setPartnerNotificationsEnabled(user.isPartnerNotificationsEnabled());
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
        String partnerEmail = normalizeEmail(dto.getPartnerEmail());
        managedUser.setPartnerEmail(partnerEmail.isBlank() ? null : partnerEmail);
        managedUser.setPartnerNotificationsEnabled(dto.isPartnerNotificationsEnabled());
        return userRepository.save(managedUser);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordDto dto) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (managedUser.getAuthProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException("Password changes are only available for email/password accounts.");
        }
        if (!passwordEncoder.matches(dto.getCurrentPassword(), managedUser.getPasswordHash())) {
            throw new IllegalArgumentException("Current password did not match.");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), managedUser.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }
        managedUser.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(managedUser);
    }

    @Transactional
    public void deleteAccount(User user, String password) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (managedUser.getAuthProvider() == AuthProvider.LOCAL) {
            if (!passwordEncoder.matches(password, managedUser.getPasswordHash())) {
                throw new IllegalArgumentException("Password confirmation did not match.");
            }
        } else if (!managedUser.getEmail().equalsIgnoreCase(password == null ? "" : password.trim())) {
            throw new IllegalArgumentException("Email confirmation did not match.");
        }

        emailVerificationTokenRepository.deleteByUser(managedUser);
        passwordResetTokenRepository.deleteByUser(managedUser);
        partnerNotificationLogRepository.deleteByUser(managedUser);
        cyclePredictionHistoryRepository.deleteByUser(managedUser);
        healthInsightRepository.deleteByUser(managedUser);
        waterLogRepository.deleteByUser(managedUser);
        sleepLogRepository.deleteByUser(managedUser);
        flowRepository.deleteByUser(managedUser);
        journalEntryRepository.deleteByUser(managedUser);
        moodRepository.deleteByUser(managedUser);
        symptomRepository.deleteByUser(managedUser);
        userConsentRepository.deleteByUser(managedUser);
        cycleRepository.deleteByUser(managedUser);
        userRepository.delete(managedUser);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private com.cyclecare.dto.ConsentAcceptanceDto toConsentAcceptance(RegistrationDto dto) {
        com.cyclecare.dto.ConsentAcceptanceDto consentDto = new com.cyclecare.dto.ConsentAcceptanceDto();
        consentDto.setAcceptedPrivacyPolicy(dto.isAcceptedPrivacyPolicy());
        consentDto.setAcceptedTerms(dto.isAcceptedTerms());
        return consentDto;
    }
}
