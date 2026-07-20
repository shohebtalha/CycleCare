package com.cyclecare.service;

import com.cyclecare.domain.User;
import com.cyclecare.domain.UserConsent;
import com.cyclecare.dto.ConsentAcceptanceDto;
import com.cyclecare.repository.UserConsentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsentService {

    private final UserConsentRepository userConsentRepository;
    private final String currentConsentVersion;

    public ConsentService(UserConsentRepository userConsentRepository,
                          @Value("${cyclecare.policy.version:2026-07-19}") String currentConsentVersion) {
        this.userConsentRepository = userConsentRepository;
        this.currentConsentVersion = currentConsentVersion;
    }

    public String currentVersion() {
        return currentConsentVersion;
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentConsent(User user) {
        return userConsentRepository.existsByUserAndConsentVersionAndAcceptedPrivacyPolicyTrueAndAcceptedTermsTrue(
                user,
                currentConsentVersion
        );
    }

    @Transactional(readOnly = true)
    public Optional<UserConsent> latestConsent(User user) {
        return userConsentRepository.findTopByUserOrderByAcceptedAtDesc(user);
    }

    @Transactional
    public UserConsent recordCurrentConsent(User user, ConsentAcceptanceDto dto) {
        if (!dto.isAcceptedPrivacyPolicy() || !dto.isAcceptedTerms()) {
            throw new IllegalArgumentException("Both Privacy Policy and Terms & Conditions must be accepted.");
        }

        UserConsent consent = new UserConsent();
        consent.setUser(user);
        consent.setConsentVersion(currentConsentVersion);
        consent.setAcceptedPrivacyPolicy(true);
        consent.setAcceptedTerms(true);
        return userConsentRepository.save(consent);
    }
}
