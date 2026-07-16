package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.User;
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
    private final CycleService cycleService = mock(CycleService.class);
    private final MailService mailService = mock(MailService.class);
    
    private final CycleNotificationService notificationService = new CycleNotificationService(
            userRepository,
            cycleService,
            mailService
    );

    @Test
    void sendUpcomingPeriodRemindersSendsEmailWhenExactlyTwoDaysAway() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("send@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("ignore@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        LocalDate targetDate = LocalDate.now().plusDays(2);

        CyclePrediction predictionTwoDays = new CyclePrediction(
                LocalDate.now().minusDays(26), 27, targetDate, LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                com.cyclecare.domain.MenstrualPhase.LUTEAL, 2, -14);

        CyclePrediction predictionThreeDays = new CyclePrediction(
                LocalDate.now().minusDays(26), 27, LocalDate.now().plusDays(3), LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(18), LocalDate.now().minusDays(12),
                com.cyclecare.domain.MenstrualPhase.LUTEAL, 3, -14);

        // First call (user1) returns 2-day prediction; second call (user2) returns 3-day prediction
        when(cycleService.currentPrediction(org.mockito.ArgumentMatchers.<List<Cycle>>any()))
                .thenReturn(Optional.of(predictionTwoDays))
                .thenReturn(Optional.of(predictionThreeDays));
        when(cycleService.recentCycles(any(User.class))).thenReturn(List.of());

        notificationService.sendUpcomingPeriodReminders();

        verify(mailService).sendPeriodReminderEmail(eq(user1), eq(targetDate));
        verify(mailService, never()).sendPeriodReminderEmail(eq(user2), any());
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
