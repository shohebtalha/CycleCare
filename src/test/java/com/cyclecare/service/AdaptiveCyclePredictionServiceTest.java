package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdaptiveCyclePredictionServiceTest {

    private final AdaptiveCyclePredictionService service = new AdaptiveCyclePredictionService();

    @Test
    void learnsFromActualPeriodStartDatesWithoutAssumingTwentyEightDays() {
        List<Cycle> cycles = cyclesFromStarts("2026-01-01", "2026-01-30", "2026-03-01", "2026-03-30");

        CyclePrediction prediction = service.predict(cycles, LocalDate.of(2026, 4, 5)).orElseThrow();

        assertThat(prediction.getStatistics().averageCycleLength()).isEqualTo(29);
        assertThat(prediction.getNextPeriodDate()).isEqualTo(LocalDate.of(2026, 4, 28));
        assertThat(prediction.getConfidence()).isEqualTo(PredictionConfidence.MEDIUM);
    }

    @Test
    void downWeightsOneAbnormalCycleSoItDoesNotDominateFuturePrediction() {
        List<Cycle> cycles = cyclesFromStarts("2026-01-01", "2026-01-30", "2026-03-01",
                "2026-03-30", "2026-05-14", "2026-06-11", "2026-07-10");

        CyclePrediction prediction = service.predict(cycles, LocalDate.of(2026, 7, 15)).orElseThrow();

        assertThat(prediction.getStatistics().outlierCycleLengths()).contains(45);
        assertThat(prediction.getStatistics().averageCycleLength()).isBetween(29, 31);
        assertThat(prediction.getNextPeriodDate()).isBefore(LocalDate.of(2026, 8, 14));
    }

    @Test
    void doesNotAssumeMenstruationStartedWhenPeriodIsLate() {
        List<Cycle> cycles = cyclesFromStarts("2026-01-01", "2026-01-30", "2026-02-28", "2026-03-29");

        CyclePrediction prediction = service.predict(cycles, LocalDate.of(2026, 5, 2)).orElseThrow();

        assertThat(prediction.isOverdue()).isTrue();
        assertThat(prediction.getOverdueDays()).isGreaterThan(0);
        assertThat(prediction.getCurrentCycleStart()).isEqualTo(LocalDate.of(2026, 3, 29));
    }

    @Test
    void lowersConfidenceForHighlyVariableCycles() {
        List<Cycle> cycles = cyclesFromStarts("2026-01-01", "2026-01-24", "2026-02-28", "2026-04-08");

        CyclePrediction prediction = service.predict(cycles, LocalDate.of(2026, 4, 12)).orElseThrow();

        assertThat(prediction.getConfidence()).isEqualTo(PredictionConfidence.LOW);
        assertThat(prediction.getStatistics().regularityScore()).isLessThan(80);
    }

    private List<Cycle> cyclesFromStarts(String... starts) {
        return List.of(starts).stream()
                .map(LocalDate::parse)
                .map(this::cycle)
                .toList();
    }

    private Cycle cycle(LocalDate start) {
        Cycle cycle = new Cycle();
        cycle.setLastPeriodStartDate(start);
        cycle.setAverageCycleLength(30);
        cycle.setAveragePeriodDuration(5);
        return cycle;
    }
}
