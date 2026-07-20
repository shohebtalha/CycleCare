package com.cyclecare.repository;

import com.cyclecare.domain.User;
import com.cyclecare.domain.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    Optional<UserConsent> findTopByUserOrderByAcceptedAtDesc(User user);

    boolean existsByUserAndConsentVersionAndAcceptedPrivacyPolicyTrueAndAcceptedTermsTrue(User user, String consentVersion);

    void deleteByUser(User user);
}
