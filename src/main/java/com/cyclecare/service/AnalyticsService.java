package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.HealthInsight;
import com.cyclecare.domain.InsightType;
import com.cyclecare.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final CycleService cycleService;
    private final MoodService moodService;
    private final SymptomService symptomService;
    private final WaterService waterService;
    private final SleepService sleepService;

    public AnalyticsService(CycleService cycleService,
                            MoodService moodService,
                            SymptomService symptomService,
                            WaterService waterService,
                            SleepService sleepService) {
        this.cycleService = cycleService;
        this.moodService = moodService;
        this.symptomService = symptomService;
        this.waterService = waterService;
        this.sleepService = sleepService;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> cycleLengthTrend(User user) {
        List<Cycle> cycles = new ArrayList<>(cycleService.recentCycles(user));
        Collections.reverse(cycles);
        Map<String, Integer> trend = new LinkedHashMap<>();
        for (Cycle cycle : cycles) {
            trend.put(cycle.getLastPeriodStartDate().toString(), cycle.getAverageCycleLength());
        }
        return trend;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> moodTrend(User user) {
        return moodService.distribution(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> symptomFrequency(User user) {
        return symptomService.frequency(user);
    }

    @Transactional(readOnly = true)
    public int regularityScore(User user) {
        List<Cycle> cycles = cycleService.recentCycles(user);
        if (cycles.size() < 2) {
            return 100;
        }
        double average = cycles.stream()
                .mapToInt(Cycle::getAverageCycleLength)
                .average()
                .orElse(28);
        double meanDeviation = cycles.stream()
                .mapToDouble(cycle -> Math.abs(cycle.getAverageCycleLength() - average))
                .average()
                .orElse(0);
        return Math.max(0, Math.min(100, (int) Math.round(100 - (meanDeviation * 8))));
    }

    @Transactional(readOnly = true)
    public List<HealthInsight> insights(User user) {
        List<HealthInsight> insights = new ArrayList<>();
        cycleService.currentPrediction(user).ifPresentOrElse(prediction -> {
            insights.add(insight(user, "Current phase: " + prediction.getPhase().getLabel(),
                    phaseMessage(prediction.getPhase().getLabel()), InsightType.INFO));
            if (prediction.getDaysUntilNextPeriod() <= 3) {
                insights.add(insight(user, "Period may start soon",
                        "Your next expected period is within three days. Consider preparing comfort supplies and tracking symptoms.", InsightType.INFO));
            }
        }, () -> insights.add(insight(user, "Add your first cycle",
                "Enter your last period start date to unlock predictions, calendar markers, and phase-based tips.", InsightType.INFO)));

        if (waterService.totalForToday(user) < 1500) {
            insights.add(insight(user, "Hydration check",
                    "Today's water total is below 1500 ml. Gentle, steady hydration may support energy and comfort.", InsightType.INFO));
        }

        if (sleepService.weeklyAverage(user) > 0 && sleepService.weeklyAverage(user) < 6) {
            insights.add(insight(user, "Sleep trend",
                    "Your seven-day sleep average is below 6 hours. Consider a calming evening routine and earlier wind-down.", InsightType.WARNING));
        }

        insights.addAll(alerts(user));
        return insights;
    }

    @Transactional(readOnly = true)
    public List<HealthInsight> alerts(User user) {
        List<HealthInsight> alerts = new ArrayList<>();
        List<Cycle> cycles = cycleService.recentCycles(user);
        boolean irregularLength = cycles.stream()
                .anyMatch(cycle -> cycle.getAverageCycleLength() < 21 || cycle.getAverageCycleLength() > 35);
        if (irregularLength) {
            alerts.add(insight(user, "Cycle irregularity noticed",
                    "One or more recorded cycle lengths falls outside the common 21-35 day range. Track patterns and consider professional guidance if this continues.", InsightType.WARNING));
        }

        cycleService.latestCycle(user).ifPresent(cycle -> {
            long gap = ChronoUnit.DAYS.between(cycle.getLastPeriodStartDate(), LocalDate.now());
            if (gap > 45) {
                alerts.add(insight(user, "Long gap between periods",
                        "It has been over 45 days since the last recorded period start. This app cannot diagnose causes; consider speaking with a qualified clinician.", InsightType.ALERT));
            }
        });

        if (symptomService.severeCountSince(user, LocalDate.now().minusDays(60)) >= 5) {
            alerts.add(insight(user, "Repeated severe symptoms",
                    "Several severe symptom entries were logged recently. Please consider professional medical support, especially if symptoms affect daily life.", InsightType.ALERT));
        }

        return alerts;
    }

    private HealthInsight insight(User user, String title, String message, InsightType type) {
        HealthInsight insight = new HealthInsight();
        insight.setUser(user);
        insight.setInsightDate(LocalDate.now());
        insight.setTitle(title);
        insight.setMessage(message);
        insight.setType(type);
        return insight;
    }

    private String phaseMessage(String phase) {
        return switch (phase) {
            case "Menstrual" -> "Warm fluids, gentle movement, and iron-rich foods may be useful during bleeding days.";
            case "Follicular" -> "Energy may rise for some people. Balanced protein and colorful foods can support activity.";
            case "Ovulation" -> "Hydration, fiber, and antioxidant-rich produce are practical supports around ovulation.";
            default -> "Steady meals, magnesium-rich foods, and calming movement may help with PMS-related discomfort.";
        };
    }
}
