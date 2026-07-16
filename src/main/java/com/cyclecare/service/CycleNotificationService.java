package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.User;
import com.cyclecare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CycleNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CycleNotificationService.class);

    private final UserRepository userRepository;
    private final CycleService cycleService;
    private final MailService mailService;

    public CycleNotificationService(UserRepository userRepository,
                                    CycleService cycleService,
                                    MailService mailService) {
        this.userRepository = userRepository;
        this.cycleService = cycleService;
        this.mailService = mailService;
    }

    /**
     * Runs daily at 8:00 AM server time.
     * Finds users whose predicted period starts in exactly 2 days and sends them an email reminder.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void sendUpcomingPeriodReminders() {
        logger.info("Starting daily upcoming period reminder job...");
        List<User> users = userRepository.findAll();
        LocalDate targetStartDate = LocalDate.now().plusDays(2);
        int emailsSent = 0;

        for (User user : users) {
            try {
                List<Cycle> recentCycles = cycleService.recentCycles(user);
                Optional<CyclePrediction> predictionOptional = cycleService.currentPrediction(recentCycles);
                
                if (predictionOptional.isPresent()) {
                    CyclePrediction prediction = predictionOptional.get();
                    if (prediction.getNextPeriodDate() != null && prediction.getNextPeriodDate().equals(targetStartDate)) {
                        mailService.sendPeriodReminderEmail(user, targetStartDate);
                        logger.info("Sent period reminder to user id {}", user.getId());
                        emailsSent++;
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to process period reminder for user id {}", user.getId(), ex);
            }
        }

        logger.info("Finished daily upcoming period reminder job. Emails sent: {}", emailsSent);
    }
}
