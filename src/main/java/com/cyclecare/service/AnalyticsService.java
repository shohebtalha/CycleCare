package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.FlowLevel;
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
import java.util.Optional;

@Service
public class AnalyticsService {

    private final CycleService cycleService;
    private final MoodService moodService;
    private final SymptomService symptomService;
    private final WaterService waterService;
    private final SleepService sleepService;
    private final FlowService flowService;

    public AnalyticsService(CycleService cycleService,
                            MoodService moodService,
                            SymptomService symptomService,
                            WaterService waterService,
                            SleepService sleepService,
                            FlowService flowService) {
        this.cycleService = cycleService;
        this.moodService = moodService;
        this.symptomService = symptomService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.flowService = flowService;
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
        Optional<CyclePrediction> prediction = cycleService.currentPrediction(user);
        int waterToday = waterService.totalForToday(user);
        double weeklySleepAverage = sleepService.weeklyAverage(user);
        List<Cycle> recentCycles = cycleService.recentCycles(user);
        List<FlowEntry> recentFlow = flowService.recent(user, 30);
        return insights(user, prediction, waterToday, weeklySleepAverage, recentCycles, recentFlow);
    }

    public List<HealthInsight> insights(User user,
                                        Optional<CyclePrediction> prediction,
                                        int waterToday,
                                        double weeklySleepAverage,
                                        List<Cycle> recentCycles,
                                        List<FlowEntry> recentFlow) {
        List<HealthInsight> insights = new ArrayList<>();
        prediction.ifPresentOrElse(cyclePrediction -> {
            insights.add(insight(user, "Current phase: " + cyclePrediction.getPhase().getLabel(),
                    phaseMessage(cyclePrediction.getPhase().getLabel()), InsightType.INFO));
            if (cyclePrediction.getDaysUntilNextPeriod() <= 3) {
                insights.add(insight(user, "Period may start soon",
                        "Your next expected period is within three days. Consider preparing comfort supplies and tracking symptoms.", InsightType.INFO));
            }
        }, () -> insights.add(insight(user, "Add your first cycle",
                "Enter your last period start date to unlock predictions, calendar markers, and phase-based tips.", InsightType.INFO)));

        if (waterToday < 1500) {
            insights.add(insight(user, "Hydration check",
                    "Today's water total is below 1500 ml. Gentle, steady hydration may support energy and comfort.", InsightType.INFO));
        }

        if (weeklySleepAverage > 0 && weeklySleepAverage < 6) {
            insights.add(insight(user, "Sleep trend",
                    "Your seven-day sleep average is below 6 hours. Consider a calming evening routine and earlier wind-down.", InsightType.WARNING));
        }

        insights.addAll(alerts(user, recentCycles, recentFlow));
        return insights;
    }

    @Transactional(readOnly = true)
    public List<HealthInsight> alerts(User user) {
        return alerts(user, cycleService.recentCycles(user), flowService.recent(user, 30));
    }

    public List<HealthInsight> alerts(User user, List<Cycle> cycles, List<FlowEntry> recentFlow) {
        List<HealthInsight> alerts = new ArrayList<>();
        boolean irregularLength = cycles.stream()
                .anyMatch(cycle -> cycle.getAverageCycleLength() < 21 || cycle.getAverageCycleLength() > 35);
        if (irregularLength) {
            alerts.add(insight(user, "Cycle irregularity noticed",
                    "One or more recorded cycle lengths falls outside the common 21-35 day range. Track patterns and consider professional guidance if this continues.", InsightType.WARNING));
        }

        cycles.stream().findFirst().ifPresent(cycle -> {
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

        if (hasConsecutiveHeavyFlow(recentFlow)) {
            alerts.add(insight(user, "Heavy flow pattern",
                    "Heavy menstrual bleeding has been recorded for several consecutive days. If this continues, consider consulting a healthcare professional.", InsightType.ALERT));
        }

        if (cycles.stream().findFirst()
                .map(cycle -> cycle.getAveragePeriodDuration() > 8)
                .orElse(false)) {
            alerts.add(insight(user, "Long period duration",
                    "Your recorded period duration exceeds 8 days. Track the pattern and consider professional guidance if this continues.", InsightType.WARNING));
        }

        if (flowService.hasRepeatedLargeClots(user)) {
            alerts.add(insight(user, "Repeated large clots",
                    "Large clots have been recorded repeatedly. Consider consulting a healthcare professional if this pattern continues.", InsightType.WARNING));
        }

        return alerts;
    }

    private boolean hasConsecutiveHeavyFlow(List<FlowEntry> entries) {
        List<FlowEntry> chronological = entries.stream()
                .sorted((left, right) -> left.getEntryDate().compareTo(right.getEntryDate()))
                .toList();
        int streak = 0;
        LocalDate previousDate = null;
        for (FlowEntry entry : chronological) {
            boolean heavy = entry.getFlowLevel() == FlowLevel.HEAVY || entry.getFlowLevel() == FlowLevel.VERY_HEAVY;
            boolean consecutive = previousDate == null || entry.getEntryDate().equals(previousDate.plusDays(1));
            streak = heavy && consecutive ? streak + 1 : (heavy ? 1 : 0);
            if (streak > 3) {
                return true;
            }
            previousDate = entry.getEntryDate();
        }
        return false;
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
