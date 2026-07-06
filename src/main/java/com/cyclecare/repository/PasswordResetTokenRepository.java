package com.cyclecare.repository;

import com.cyclecare.domain.PasswordResetToken;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserAndUsedFalse(User user);
}
