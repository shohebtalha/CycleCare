package com.cyclecare.service;

import com.cyclecare.domain.MenstrualPhase;

import java.time.LocalDate;

public class CyclePrediction {

    private final LocalDate currentCycleStart;
    private final int currentCycleDay;
    private final LocalDate nextPeriodDate;
    private final LocalDate ovulationDate;
    private final LocalDate fertilityWindowStart;
    private final LocalDate fertilityWindowEnd;
    private final MenstrualPhase phase;
    private final long daysUntilNextPeriod;
    private final long daysUntilOvulation;
    private final long overdueDays;
    private final PredictionConfidence confidence;
    private final CycleStatistics statistics;
    private final String explanation;

    public CyclePrediction(LocalDate currentCycleStart,
                           int currentCycleDay,
                           LocalDate nextPeriodDate,
                           LocalDate ovulationDate,
                           LocalDate fertilityWindowStart,
                           LocalDate fertilityWindowEnd,
                           MenstrualPhase phase,
                           long daysUntilNextPeriod,
                           long daysUntilOvulation) {
        this(currentCycleStart, currentCycleDay, nextPeriodDate, ovulationDate, fertilityWindowStart,
                fertilityWindowEnd, phase, daysUntilNextPeriod, daysUntilOvulation, 0,
                PredictionConfidence.LOW, null,
                "Prediction is based on the available cycle length estimate.");
    }

    public CyclePrediction(LocalDate currentCycleStart,
                           int currentCycleDay,
                           LocalDate nextPeriodDate,
                           LocalDate ovulationDate,
                           LocalDate fertilityWindowStart,
                           LocalDate fertilityWindowEnd,
                           MenstrualPhase phase,
                           long daysUntilNextPeriod,
                           long daysUntilOvulation,
                           long overdueDays,
                           PredictionConfidence confidence,
                           CycleStatistics statistics,
                           String explanation) {
        this.currentCycleStart = currentCycleStart;
        this.currentCycleDay = currentCycleDay;
        this.nextPeriodDate = nextPeriodDate;
        this.ovulationDate = ovulationDate;
        this.fertilityWindowStart = fertilityWindowStart;
        this.fertilityWindowEnd = fertilityWindowEnd;
        this.phase = phase;
        this.daysUntilNextPeriod = daysUntilNextPeriod;
        this.daysUntilOvulation = daysUntilOvulation;
        this.overdueDays = overdueDays;
        this.confidence = confidence;
        this.statistics = statistics;
        this.explanation = explanation;
    }

    public LocalDate getCurrentCycleStart() {
        return currentCycleStart;
    }

    public int getCurrentCycleDay() {
        return currentCycleDay;
    }

    public LocalDate getNextPeriodDate() {
        return nextPeriodDate;
    }

    public LocalDate getOvulationDate() {
        return ovulationDate;
    }

    public LocalDate getFertilityWindowStart() {
        return fertilityWindowStart;
    }

    public LocalDate getFertilityWindowEnd() {
        return fertilityWindowEnd;
    }

    public MenstrualPhase getPhase() {
        return phase;
    }

    public long getDaysUntilNextPeriod() {
        return daysUntilNextPeriod;
    }

    public long getDaysUntilOvulation() {
        return daysUntilOvulation;
    }

    public long getOverdueDays() {
        return overdueDays;
    }

    public boolean isOverdue() {
        return overdueDays > 0;
    }

    public PredictionConfidence getConfidence() {
        return confidence;
    }

    public CycleStatistics getStatistics() {
        return statistics;
    }

    public String getExplanation() {
        return explanation;
    }
}
