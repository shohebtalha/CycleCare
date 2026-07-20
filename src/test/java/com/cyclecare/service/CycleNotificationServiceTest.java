package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.User;
import com.cyclecare.repository.PartnerNotificationLogRepository;
import com.cyclecare.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CycleNotificationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PartnerNotificationLogRepository partnerNotificationLogRepository = mock(PartnerNotificationLogRepository.class);
    private final CycleService cycleService = mock(CycleService.class);
    private final MailService mailService = mock(MailService.class);
    
    private final CycleNotificationService notificationService = new CycleNotificationService(
            userRepository,
            partnerNotificationLogRepository,
            cycleService,
            mailService
    );

    @Test
    void sendUpcomingPeriodRemindersSendsEmailWhenPeriodIsWithinTwoDays() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("send@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("ignore@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        LocalDate targetDate = LocalDate.now().plusDays(1);

        CyclePrediction predictionOneDay = new CyclePrediction(
                LocalDate.now().minusDays(26), 27, targetDate, LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                com.cyclecare.domain.MenstrualPhase.LUTEAL, 1, -14);

        CyclePrediction predictionThreeDays = new CyclePrediction(
                LocalDate.now().minusDays(26), 27, LocalDate.now().plusDays(3), LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                com.cyclecare.domain.MenstrualPhase.LUTEAL, 3, -14);

        // First call (user1) returns in-window prediction; second call (user2) returns 3-day prediction.
        when(cycleService.currentPrediction(org.mockito.ArgumentMatchers.<List<Cycle>>any()))
                .thenReturn(Optional.of(predictionOneDay))
                .thenReturn(Optional.of(predictionThreeDays));
        when(cycleService.recentCycles(any(User.class))).thenReturn(List.of());

        notificationService.sendUpcomingPeriodReminders();

        verify(mailService).sendPeriodReminderEmail(eq(user1), eq(targetDate));
        verify(mailService, never()).sendPeriodReminderEmail(eq(user2), any());
    }

    @Test
    void sendUpcomingPeriodRemindersSendsPartnerEmailOnlyWhenOptedInAndNotAlreadySent() {
        User optedInUser = new User();
        optedInUser.setId(1L);
        optedInUser.setEmail("user@example.com");
        optedInUser.setPartnerEmail("partner@example.com");
        optedInUser.setPartnerNotificationsEnabled(true);

        User optedOutUser = new User();
        optedOutUser.setId(2L);
        optedOutUser.setEmail("private@example.com");
        optedOutUser.setPartnerEmail("partner2@example.com");
        optedOutUser.setPartnerNotificationsEnabled(false);

        when(userRepository.findAll()).thenReturn(List.of(optedInUser, optedOutUser));
        LocalDate targetDate = LocalDate.now().plusDays(2);
        CyclePrediction prediction = new CyclePrediction(
                LocalDate.now().minusDays(26), 27, targetDate, LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                com.cyclecare.domain.MenstrualPhase.LUTEAL, 2, -14);

        when(cycleService.recentCycles(any(User.class))).thenReturn(List.of());
        when(cycleService.currentPrediction(org.mockito.ArgumentMatchers.<List<Cycle>>any()))
                .thenReturn(Optional.of(prediction));
        when(partnerNotificationLogRepository.existsByUserAndPartnerEmailIgnoreCaseAndPredictedPeriodDateAndNotificationType(
                eq(optedInUser), eq("partner@example.com"), eq(targetDate), eq("PARTNER_PERIOD_REMINDER")))
                .thenReturn(false);

        notificationService.sendUpcomingPeriodReminders();

        verify(mailService).sendPartnerPeriodReminderEmail(optedInUser, "partner@example.com", targetDate);
        verify(mailService, never()).sendPartnerPeriodReminderEmail(eq(optedOutUser), any(), any());
        verify(partnerNotificationLogRepository).save(any());
    }

    @Test
    void sendUpcomingPeriodRemindersSkipsAlreadySentPartnerReminder() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPartnerEmail("partner@example.com");
        user.setPartnerNotificationsEnabled(true);
        when(userRepository.findAll()).thenReturn(List.of(user));
        LocalDate targetDate = LocalDate.now().plusDays(2);
        CyclePrediction prediction = new CyclePrediction(
                LocalDate.now().minusDays(26), 27, targetDate, LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                com.cyclecare.domain.MenstrualPhase.LUTEAL, 2, -14);

        when(cycleService.recentCycles(user)).thenReturn(List.of());
        when(cycleService.currentPrediction(org.mockito.ArgumentMatchers.<List<Cycle>>any()))
                .thenReturn(Optional.of(prediction));
        when(partnerNotificationLogRepository.existsByUserAndPartnerEmailIgnoreCaseAndPredictedPeriodDateAndNotificationType(
                eq(user), eq("partner@example.com"), eq(targetDate), eq("PARTNER_PERIOD_REMINDER")))
                .thenReturn(true);

        notificationService.sendUpcomingPeriodReminders();

        verify(mailService, never()).sendPartnerPeriodReminderEmail(any(), any(), any());
        verify(partnerNotificationLogRepository, never()).save(any());
    }

    @Test
    void sendUpcomingPeriodRemindersIgnoresMissingPredictions() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(cycleService.recentCycles(user)).thenReturn(List.of());
        when(cycleService.currentPrediction(org.mockito.ArgumentMatchers.<List<Cycle>>any())).thenReturn(Optional.empty());

        notificationService.sendUpcomingPeriodReminders();

        verify(mailService, never()).sendPeriodReminderEmail(any(), any());
    }
}
