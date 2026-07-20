package com.cyclecare.repository;

import com.cyclecare.domain.EmailVerificationToken;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByUserAndUsedFalse(User user);

    void deleteByUser(User user);
}
