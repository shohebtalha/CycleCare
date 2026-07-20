package com.cyclecare.repository;

import com.cyclecare.domain.PartnerNotificationLog;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PartnerNotificationLogRepository extends JpaRepository<PartnerNotificationLog, Long> {

    boolean existsByUserAndPartnerEmailIgnoreCaseAndPredictedPeriodDateAndNotificationType(
            User user, String partnerEmail, LocalDate predictedPeriodDate, String notificationType);

    void deleteByUser(User user);
}
