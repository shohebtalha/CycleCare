package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.PartnerNotificationLog;
import com.cyclecare.domain.User;
import com.cyclecare.repository.PartnerNotificationLogRepository;
import com.cyclecare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CycleNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CycleNotificationService.class);
    private static final String PARTNER_PERIOD_REMINDER = "PARTNER_PERIOD_REMINDER";

    private final UserRepository userRepository;
    private final PartnerNotificationLogRepository partnerNotificationLogRepository;
    private final CycleService cycleService;
    private final MailService mailService;

    public CycleNotificationService(UserRepository userRepository,
                                    PartnerNotificationLogRepository partnerNotificationLogRepository,
                                    CycleService cycleService,
                                    MailService mailService) {
        this.userRepository = userRepository;
        this.partnerNotificationLogRepository = partnerNotificationLogRepository;
        this.cycleService = cycleService;
        this.mailService = mailService;
    }

    /**
     * Runs daily at 8:00 AM server time.
     * Finds users whose predicted period starts within the next 2 days and sends one reminder per cycle.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void sendUpcomingPeriodReminders() {
        logger.info("Starting daily upcoming period reminder job...");
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now();
        int emailsSent = 0;
        int partnerEmailsSent = 0;

        for (User user : users) {
            try {
                List<Cycle> recentCycles = cycleService.recentCycles(user);
                Optional<CyclePrediction> predictionOptional = cycleService.currentPrediction(recentCycles);
                
                if (predictionOptional.isPresent()) {
                    CyclePrediction prediction = predictionOptional.get();
                    LocalDate nextPeriodDate = prediction.getNextPeriodDate();
                    if (isReminderWindow(today, nextPeriodDate)) {
                        mailService.sendPeriodReminderEmail(user, nextPeriodDate);
                        logger.info("Sent period reminder to user id {}", user.getId());
                        emailsSent++;
                        if (sendPartnerReminderIfEnabled(user, nextPeriodDate)) {
                            partnerEmailsSent++;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to process period reminder for user id {}", user.getId(), ex);
            }
        }

        logger.info("Finished daily upcoming period reminder job. User emails sent: {}, partner emails sent: {}",
                emailsSent, partnerEmailsSent);
    }

    private boolean isReminderWindow(LocalDate today, LocalDate predictedPeriodDate) {
        if (predictedPeriodDate == null) {
            return false;
        }
        long daysUntilPeriod = ChronoUnit.DAYS.between(today, predictedPeriodDate);
        return daysUntilPeriod >= 0 && daysUntilPeriod <= 2;
    }

    private boolean sendPartnerReminderIfEnabled(User user, LocalDate predictedPeriodDate) {
        String partnerEmail = user.getPartnerEmail();
        if (!user.isPartnerNotificationsEnabled() || partnerEmail == null || partnerEmail.isBlank()) {
            return false;
        }
        if (partnerNotificationLogRepository.existsByUserAndPartnerEmailIgnoreCaseAndPredictedPeriodDateAndNotificationType(
                user, partnerEmail, predictedPeriodDate, PARTNER_PERIOD_REMINDER)) {
            logger.info("Skipping duplicate partner reminder for user id {} and predicted date {}",
                    user.getId(), predictedPeriodDate);
            return false;
        }

        mailService.sendPartnerPeriodReminderEmail(user, partnerEmail, predictedPeriodDate);
        PartnerNotificationLog log = new PartnerNotificationLog();
        log.setUser(user);
        log.setPartnerEmail(partnerEmail);
        log.setPredictedPeriodDate(predictedPeriodDate);
        log.setNotificationType(PARTNER_PERIOD_REMINDER);
        partnerNotificationLogRepository.save(log);
        logger.info("Sent partner period reminder for user id {}", user.getId());
        return true;
    }
}
