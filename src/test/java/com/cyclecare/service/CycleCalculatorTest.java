package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.MenstrualPhase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CycleCalculatorTest {

    private final CycleCalculator calculator = new CycleCalculator();

    @Test
    void calculatesMenstrualPhaseAndPredictionDates() {
        Cycle cycle = cycle(LocalDate.of(2026, 6, 1), 28, 5);

        CyclePrediction prediction = calculator.calculate(cycle, LocalDate.of(2026, 6, 3));

        assertThat(prediction.getCurrentCycleDay()).isEqualTo(3);
        assertThat(prediction.getNextPeriodDate()).isEqualTo(LocalDate.of(2026, 6, 29));
        assertThat(prediction.getOvulationDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(prediction.getFertilityWindowStart()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(prediction.getFertilityWindowEnd()).isEqualTo(LocalDate.of(2026, 6, 16));
        assertThat(prediction.getPhase()).isEqualTo(MenstrualPhase.MENSTRUAL);
    }

    @Test
    void marksOvulationPhaseAroundOvulationDate() {
        Cycle cycle = cycle(LocalDate.of(2026, 6, 1), 28, 5);

        CyclePrediction prediction = calculator.calculate(cycle, LocalDate.of(2026, 6, 15));

        assertThat(prediction.getPhase()).isEqualTo(MenstrualPhase.OVULATION);
        assertThat(prediction.getDaysUntilOvulation()).isZero();
    }

    @Test
    void calculatesNextOvulationCountdownAfterCurrentOvulationPasses() {
        Cycle cycle = cycle(LocalDate.of(2026, 6, 1), 28, 5);

        CyclePrediction prediction = calculator.calculate(cycle, LocalDate.of(2026, 6, 22));

        assertThat(prediction.getPhase()).isEqualTo(MenstrualPhase.LUTEAL);
        assertThat(prediction.getDaysUntilOvulation()).isEqualTo(21);
    }

    private Cycle cycle(LocalDate start, int length, int duration) {
        Cycle cycle = new Cycle();
        cycle.setLastPeriodStartDate(start);
        cycle.setAverageCycleLength(length);
        cycle.setAveragePeriodDuration(duration);
        return cycle;
    }
}
