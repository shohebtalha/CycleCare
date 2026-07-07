package com.cyclecare.service;

import com.cyclecare.domain.CyclePredictionHistory;
import com.cyclecare.domain.InsightType;
import com.cyclecare.domain.User;
import com.cyclecare.dto.HealthIntelligenceView;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthIntelligenceServiceTest {

    @Test
    void createsHealthAlertWhenPredictedAndActualPeriodDatesDoNotMatch() {
        CycleService cycleService = mock(CycleService.class);
        FlowService flowService = mock(FlowService.class);
        SymptomService symptomService = mock(SymptomService.class);
        MoodService moodService = mock(MoodService.class);
        SleepService sleepService = mock(SleepService.class);
        WaterService waterService = mock(WaterService.class);
        JournalService journalService = mock(JournalService.class);
        AnalyticsService analyticsService = mock(AnalyticsService.class);

        User user = new User();
        CyclePredictionHistory miss = new CyclePredictionHistory();
        miss.setPredictedPeriodDate(LocalDate.of(2026, 7, 22));
        miss.setActualPeriodStartDate(LocalDate.of(2026, 7, 27));
        miss.setPredictedCycleLength(28);
        miss.setActualCycleLength(33);
        miss.setPredictionErrorDays(5);
        miss.setConfidence("MEDIUM");
        miss.setExplanation("Recent cycles shifted.");

        when(cycleService.recentCycles(user)).thenReturn(List.of());
        when(cycleService.currentPrediction(any(List.class))).thenReturn(Optional.empty());
        when(cycleService.predictionHistory(user)).thenReturn(List.of(miss));
        when(flowService.between(any(), any(), any())).thenReturn(List.of());
        when(symptomService.between(any(), any(), any())).thenReturn(List.of());
        when(moodService.between(any(), any(), any())).thenReturn(List.of());
        when(sleepService.between(any(), any(), any())).thenReturn(List.of());
        when(waterService.between(any(), any(), any())).thenReturn(List.of());
        when(journalService.between(any(), any(), any())).thenReturn(List.of());
        when(analyticsService.alerts(any(), any(), any())).thenReturn(List.of());

        HealthIntelligenceService service = new HealthIntelligenceService(
                cycleService,
                flowService,
                symptomService,
                moodService,
                sleepService,
                waterService,
                journalService,
                analyticsService
        );

        HealthIntelligenceView view = service.build(user);

        assertThat(view.alerts())
                .anySatisfy(alert -> {
                    assertThat(alert.title()).isEqualTo("Prediction did not match actual period");
                    assertThat(alert.type()).isEqualTo(InsightType.ALERT);
                    assertThat(alert.message()).contains("2026-07-22", "2026-07-27", "5 day(s) later");
                });
        assertThat(view.trends())
                .anySatisfy(trend -> assertThat(trend.title()).isEqualTo("Prediction accuracy trend"));
        assertThat(view.recommendations())
                .anySatisfy(recommendation -> assertThat(recommendation.title()).isEqualTo("Keep confirming actual start dates"));
    }
}
