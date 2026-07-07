package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.CyclePredictionHistory;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.FlowLevel;
import com.cyclecare.domain.InsightType;
import com.cyclecare.domain.JournalEntry;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.MoodType;
import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.SymptomType;
import com.cyclecare.domain.User;
import com.cyclecare.domain.WaterLog;
import com.cyclecare.dto.HealthIntelligenceCard;
import com.cyclecare.dto.HealthIntelligenceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HealthIntelligenceService {

    private static final int HYDRATION_TARGET_ML = 2000;

    private final CycleService cycleService;
    private final FlowService flowService;
    private final SymptomService symptomService;
    private final MoodService moodService;
    private final SleepService sleepService;
    private final WaterService waterService;
    private final JournalService journalService;
    private final AnalyticsService analyticsService;

    public HealthIntelligenceService(CycleService cycleService,
                                     FlowService flowService,
                                     SymptomService symptomService,
                                     MoodService moodService,
                                     SleepService sleepService,
                                     WaterService waterService,
                                     JournalService journalService,
                                     AnalyticsService analyticsService) {
        this.cycleService = cycleService;
        this.flowService = flowService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.sleepService = sleepService;
        this.waterService = waterService;
        this.journalService = journalService;
        this.analyticsService = analyticsService;
    }

    @Transactional(readOnly = true)
    public HealthIntelligenceView build(User user) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(90);

        List<Cycle> cycles = cycleService.recentCycles(user);
        List<FlowEntry> flowEntries = flowService.between(user, start, today);
        List<Symptom> symptoms = symptomService.between(user, start, today);
        List<Mood> moods = moodService.between(user, start, today);
        List<SleepLog> sleepLogs = sleepService.between(user, start, today);
        List<WaterLog> waterLogs = waterService.between(user, start, today);
        List<JournalEntry> journalEntries = journalService.between(user, start, today);
        List<CyclePredictionHistory> predictionHistory = cycleService.predictionHistory(user);
        Optional<CyclePrediction> prediction = cycleService.currentPrediction(cycles);

        List<HealthIntelligenceCard> alerts = analyticsService.alerts(user, cycles, flowEntries).stream()
                .map(this::card)
                .collect(Collectors.toCollection(ArrayList::new));
        List<HealthIntelligenceCard> trends = new ArrayList<>();
        List<HealthIntelligenceCard> correlations = new ArrayList<>();
        List<HealthIntelligenceCard> recommendations = new ArrayList<>();

        addCycleTrends(cycles, trends, alerts);
        addFlowTrends(flowEntries, trends, alerts, recommendations);
        addPredictionAccuracyPatterns(predictionHistory, alerts, trends, recommendations);
        addSleepHydrationPatterns(flowEntries, sleepLogs, waterLogs, trends, correlations, recommendations);
        addSymptomPatterns(symptoms, cycles, alerts, correlations, recommendations);
        addMoodPatterns(moods, journalEntries, correlations, recommendations);

        prediction.ifPresentOrElse(cyclePrediction -> recommendations.add(card(
                "Phase-aware support",
                phaseRecommendation(cyclePrediction),
                InsightType.INFO,
                "sparkles",
                "Cycle phase"
        )), () -> recommendations.add(card(
                "Start cycle tracking",
                "Add your latest period start date so CycleCare can calculate phase-aware insights instead of generic wellness tips.",
                InsightType.INFO,
                "calendar-plus",
                "Setup"
        )));

        ensureMinimumCards(trends, correlations, recommendations);

        int confidence = predictionConfidence(cycles, symptoms, moods, sleepLogs, waterLogs, flowEntries);
        int healthScore = healthScore(confidence, alerts, sleepLogs, waterLogs, symptoms);

        return new HealthIntelligenceView(
                healthScore,
                confidence,
                cyclePattern(cycles),
                prediction.map(value -> value.getPhase().getLabel() + " phase, about "
                        + value.getDaysUntilNextPeriod() + " day(s) until the next expected period.")
                        .orElse("Add cycle history to unlock phase summaries."),
                alerts,
                trends,
                correlations,
                recommendations
        );
    }

    private void addPredictionAccuracyPatterns(List<CyclePredictionHistory> predictionHistory,
                                               List<HealthIntelligenceCard> alerts,
                                               List<HealthIntelligenceCard> trends,
                                               List<HealthIntelligenceCard> recommendations) {
        List<CyclePredictionHistory> missedPredictions = predictionHistory.stream()
                .filter(history -> history.getPredictionErrorDays() != null && history.getPredictionErrorDays() != 0)
                .toList();

        if (missedPredictions.isEmpty()) {
            return;
        }

        CyclePredictionHistory latestMiss = missedPredictions.get(0);
        int latestError = latestMiss.getPredictionErrorDays();
        int absoluteLatestError = Math.abs(latestError);
        InsightType type = absoluteLatestError >= 5 ? InsightType.ALERT : InsightType.WARNING;

        alerts.add(card(
                "Prediction did not match actual period",
                "CycleCare predicted " + latestMiss.getPredictedPeriodDate()
                        + ", but your period was logged on " + latestMiss.getActualPeriodStartDate()
                        + ". That is " + absoluteLatestError + " day(s) "
                        + (latestError > 0 ? "later" : "earlier")
                        + " than expected, so future predictions have been recalculated from the confirmed start date.",
                type,
                "calendar-x",
                "Prediction accuracy"
        ));

        double averageError = missedPredictions.stream()
                .mapToInt(history -> Math.abs(history.getPredictionErrorDays()))
                .average()
                .orElse(0);
        trends.add(card(
                "Prediction accuracy trend",
                "Across recent prediction misses, the average difference is about " + rounded(averageError)
                        + " day(s). More confirmed period starts will help CycleCare learn whether this is a shift or occasional variation.",
                InsightType.INFO,
                "target",
                "Cycle prediction"
        ));

        if (missedPredictions.size() >= 2) {
            alerts.add(card(
                    "Repeated prediction mismatch",
                    "Your last " + missedPredictions.size()
                            + " prediction miss(es) suggest your cycle timing is changing or variable. This is not a diagnosis, but it is worth watching the pattern.",
                    InsightType.WARNING,
                    "repeat-2",
                    "Cycle variability"
            ));
        }

        recommendations.add(card(
                "Keep confirming actual start dates",
                "When your period starts earlier or later than predicted, log the actual start date. CycleCare cancels the old prediction and learns from the confirmed date.",
                InsightType.INFO,
                "calendar-check",
                "Prediction tuning"
        ));
    }

    private void addCycleTrends(List<Cycle> cycles,
                                List<HealthIntelligenceCard> trends,
                                List<HealthIntelligenceCard> alerts) {
        if (cycles.size() < 3) {
            return;
        }

        List<Cycle> chronological = chronological(cycles, Cycle::getLastPeriodStartDate);
        double firstHalfAverage = chronological.subList(0, chronological.size() / 2).stream()
                .mapToInt(this::cycleLength)
                .average()
                .orElse(0);
        double secondHalfAverage = chronological.subList(chronological.size() / 2, chronological.size()).stream()
                .mapToInt(this::cycleLength)
                .average()
                .orElse(0);
        double shift = secondHalfAverage - firstHalfAverage;

        if (Math.abs(shift) >= 3) {
            trends.add(card(
                    "Cycle length is shifting",
                    "Your recent cycle length is averaging " + rounded(secondHalfAverage)
                            + " days versus " + rounded(firstHalfAverage)
                            + " days earlier. Keep tracking to confirm whether this is a stable body pattern.",
                    InsightType.WARNING,
                    "activity",
                    "Cycle trend"
            ));
        }

        int shortest = chronological.stream().mapToInt(this::cycleLength).min().orElse(0);
        int longest = chronological.stream().mapToInt(this::cycleLength).max().orElse(0);
        if (longest - shortest >= 7) {
            alerts.add(card(
                    "High cycle variability",
                    "Your recorded cycle lengths vary by " + (longest - shortest)
                            + " days. Irregular cycles can happen, but persistent changes are worth discussing with a clinician.",
                    InsightType.WARNING,
                    "alert-triangle",
                    "Cycle alert"
            ));
        }
    }

    private void addFlowTrends(List<FlowEntry> flowEntries,
                               List<HealthIntelligenceCard> trends,
                               List<HealthIntelligenceCard> alerts,
                               List<HealthIntelligenceCard> recommendations) {
        long heavyDays = flowEntries.stream().filter(this::isHeavy).count();
        if (heavyDays >= 3) {
            trends.add(card(
                    "Heavy flow days detected",
                    "You logged " + heavyDays
                            + " heavy or very heavy flow day(s) in the last 90 days. CycleCare will pair this with nutrition and fatigue patterns.",
                    InsightType.WARNING,
                    "droplets",
                    "Blood flow"
            ));
            recommendations.add(card(
                    "Prioritize iron-supportive meals",
                    "On heavier flow days, consider iron-rich foods with vitamin C sources. This is supportive guidance, not medical treatment.",
                    InsightType.INFO,
                    "utensils",
                    "Nutrition"
            ));
        }

        if (hasProgressivelyHeavierFlow(flowEntries)) {
            alerts.add(card(
                    "Flow appears to be increasing",
                    "Recent flow entries trend heavier than earlier entries. If this continues or affects daily life, consider professional guidance.",
                    InsightType.ALERT,
                    "trending-up",
                    "Blood flow alert"
            ));
        }
    }

    private void addSleepHydrationPatterns(List<FlowEntry> flowEntries,
                                           List<SleepLog> sleepLogs,
                                           List<WaterLog> waterLogs,
                                           List<HealthIntelligenceCard> trends,
                                           List<HealthIntelligenceCard> correlations,
                                           List<HealthIntelligenceCard> recommendations) {
        double sleepOnFlowDays = averageOnDates(sleepLogs, flowEntries.stream()
                .map(FlowEntry::getEntryDate)
                .collect(Collectors.toSet()));
        double overallSleep = sleepLogs.stream().mapToDouble(SleepLog::getHours).average().orElse(0);
        if (sleepOnFlowDays > 0 && overallSleep > 0 && overallSleep - sleepOnFlowDays >= 1) {
            correlations.add(card(
                    "Sleep drops during bleeding days",
                    "Your sleep on logged flow days averages " + rounded(sleepOnFlowDays)
                            + " hours versus " + rounded(overallSleep)
                            + " hours overall.",
                    InsightType.INFO,
                    "moon",
                    "Sleep + flow"
            ));
            recommendations.add(card(
                    "Protect rest around period days",
                    "Plan lower-friction evenings around expected bleeding days: earlier wind-down, heat support, and lighter commitments where possible.",
                    InsightType.INFO,
                    "bed",
                    "Sleep"
            ));
        }

        double periodHydration = averageWaterOnDates(waterLogs, flowEntries.stream()
                .map(FlowEntry::getEntryDate)
                .collect(Collectors.toSet()));
        double overallHydration = waterLogs.stream().collect(Collectors.groupingBy(
                        WaterLog::getEntryDate,
                        Collectors.summingInt(WaterLog::getAmountMl)))
                .values().stream().mapToInt(Integer::intValue).average().orElse(0);
        if (periodHydration > 0 && periodHydration < HYDRATION_TARGET_ML && periodHydration + 250 < overallHydration) {
            correlations.add(card(
                    "Hydration dips during periods",
                    "Your average water intake on flow days is " + rounded(periodHydration)
                            + " ml, lower than your broader logged average.",
                    InsightType.INFO,
                    "glass-water",
                    "Water + cycle"
            ));
        }

        if (overallHydration > 0 && overallHydration < HYDRATION_TARGET_ML) {
            trends.add(card(
                    "Hydration below target",
                    "Your logged daily hydration averages " + rounded(overallHydration)
                            + " ml. Small, steady increases may support headaches, cramps, and energy.",
                    InsightType.INFO,
                    "droplet",
                    "Hydration"
            ));
        }
    }

    private void addSymptomPatterns(List<Symptom> symptoms,
                                    List<Cycle> cycles,
                                    List<HealthIntelligenceCard> alerts,
                                    List<HealthIntelligenceCard> correlations,
                                    List<HealthIntelligenceCard> recommendations) {
        Map<SymptomType, Long> frequentSymptoms = symptoms.stream()
                .filter(symptom -> symptom.getType() != SymptomType.NO_SYMPTOM)
                .collect(Collectors.groupingBy(Symptom::getType, LinkedHashMap::new, Collectors.counting()));

        frequentSymptoms.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() >= 3)
                .ifPresent(entry -> correlations.add(card(
                        "Recurring " + entry.getKey().getLabel().toLowerCase(),
                        entry.getKey().getLabel() + " appears " + entry.getValue()
                                + " time(s) in recent logs. Tracking timing around your cycle can reveal whether it is phase-linked.",
                        InsightType.INFO,
                        "stethoscope",
                        "Symptoms"
                )));

        long severeSymptoms = symptoms.stream().filter(symptom -> symptom.getSeverity() >= 4).count();
        if (severeSymptoms >= 4) {
            alerts.add(card(
                    "Severe symptoms are repeating",
                    "You logged severe symptoms " + severeSymptoms
                            + " time(s) recently. CycleCare cannot diagnose, but repeated severe symptoms deserve attention.",
                    InsightType.ALERT,
                    "circle-alert",
                    "Symptom alert"
            ));
        }

        if (hasPremenstrualHeadaches(symptoms, cycles)) {
            correlations.add(card(
                    "Headaches appear before periods",
                    "Headaches are showing up close to recorded period starts. Hydration, sleep, and stress logs may help explain the pattern.",
                    InsightType.INFO,
                    "brain",
                    "Headache pattern"
            ));
            recommendations.add(card(
                    "Prepare for recurring headaches",
                    "When the calendar shows the late luteal window, watch hydration and sleep and record severity so the pattern becomes clearer.",
                    InsightType.INFO,
                    "clipboard-list",
                    "Planning"
            ));
        }
    }

    private void addMoodPatterns(List<Mood> moods,
                                 List<JournalEntry> journalEntries,
                                 List<HealthIntelligenceCard> correlations,
                                 List<HealthIntelligenceCard> recommendations) {
        long stressLikeMoods = moods.stream()
                .filter(mood -> mood.getType() == MoodType.STRESSED || mood.getType() == MoodType.ANXIOUS
                        || mood.getType() == MoodType.IRRITATED)
                .count();
        if (stressLikeMoods >= 3) {
            correlations.add(card(
                    "Stress-linked moods are repeating",
                    "Stress, anxiety, or irritation appear " + stressLikeMoods
                            + " time(s) in recent mood logs. Journal notes can help identify triggers.",
                    InsightType.INFO,
                    "heart-pulse",
                    "Mood + journal"
            ));
        }

        long nutritionEntries = journalEntries.stream()
                .filter(entry -> entry.getNutritionLog() != null && !entry.getNutritionLog().isBlank())
                .count();
        if (nutritionEntries >= 3 && !moods.isEmpty()) {
            recommendations.add(card(
                    "Review meals on low-mood days",
                    "Check the meals logged on days you felt stressed, anxious, or low. Repeated patterns can help you plan steadier snacks and hydration.",
                    InsightType.INFO,
                    "notebook-tabs",
                    "Nutrition + mood"
            ));
        }
    }

    private void ensureMinimumCards(List<HealthIntelligenceCard> trends,
                                    List<HealthIntelligenceCard> correlations,
                                    List<HealthIntelligenceCard> recommendations) {
        if (trends.isEmpty()) {
            trends.add(card(
                    "Keep building your baseline",
                    "CycleCare becomes more personal after a few cycles plus regular wellness logs. Your future insights will compare against your own body pattern.",
                    InsightType.INFO,
                    "chart-no-axes-combined",
                    "Baseline"
            ));
        }
        if (correlations.isEmpty()) {
            correlations.add(card(
                    "Cross-feature patterns need more overlap",
                    "Log wellness data on the same days as cycle, symptom, and mood entries so CycleCare can detect relationships instead of isolated events.",
                    InsightType.INFO,
                    "network",
                    "Correlation"
            ));
        }
        if (recommendations.isEmpty()) {
            recommendations.add(card(
                    "Log with intent",
                    "A useful weekly rhythm is cycle status, flow when relevant, symptoms, mood, water, sleep, and a short journal note.",
                    InsightType.INFO,
                    "list-checks",
                    "Habit"
            ));
        }
    }

    private int predictionConfidence(List<Cycle> cycles,
                                     List<Symptom> symptoms,
                                     List<Mood> moods,
                                     List<SleepLog> sleepLogs,
                                     List<WaterLog> waterLogs,
                                     List<FlowEntry> flowEntries) {
        int score = 20;
        score += Math.min(35, cycles.size() * 7);
        score += Math.min(15, flowEntries.size() * 2);
        score += Math.min(10, symptoms.size());
        score += Math.min(10, moods.size());
        score += Math.min(10, (sleepLogs.size() + waterLogs.size()) / 3);
        return clamp(score);
    }

    private int healthScore(int confidence,
                            List<HealthIntelligenceCard> alerts,
                            List<SleepLog> sleepLogs,
                            List<WaterLog> waterLogs,
                            List<Symptom> symptoms) {
        int score = 45 + confidence / 3;
        score -= alerts.stream().mapToInt(alert -> alert.type() == InsightType.ALERT ? 12 : 6).sum();
        if (sleepLogs.stream().mapToDouble(SleepLog::getHours).average().orElse(7) < 6) {
            score -= 8;
        }
        if (waterLogs.stream().collect(Collectors.groupingBy(WaterLog::getEntryDate,
                        Collectors.summingInt(WaterLog::getAmountMl)))
                .values().stream().mapToInt(Integer::intValue).average().orElse(HYDRATION_TARGET_ML) < 1500) {
            score -= 6;
        }
        if (symptoms.stream().filter(symptom -> symptom.getSeverity() >= 4).count() >= 4) {
            score -= 8;
        }
        return clamp(score);
    }

    private String cyclePattern(List<Cycle> cycles) {
        if (cycles.isEmpty()) {
            return "No cycle baseline yet.";
        }
        List<Integer> lengths = cycles.stream()
                .map(cycle -> cycle.getActualCycleLength() != null ? cycle.getActualCycleLength() : cycle.getAverageCycleLength())
                .toList();
        double average = lengths.stream().mapToInt(Integer::intValue).average().orElse(0);
        int shortest = lengths.stream().mapToInt(Integer::intValue).min().orElse((int) average);
        int longest = lengths.stream().mapToInt(Integer::intValue).max().orElse((int) average);
        return "Average " + rounded(average) + " days, range " + shortest + "-" + longest + " days across "
                + cycles.size() + " recorded cycle(s).";
    }

    private int cycleLength(Cycle cycle) {
        return cycle.getActualCycleLength() != null ? cycle.getActualCycleLength() : cycle.getAverageCycleLength();
    }

    private String phaseRecommendation(CyclePrediction prediction) {
        return switch (prediction.getPhase()) {
            case MENSTRUAL -> "You are in the menstrual phase. Gentle movement, iron-supportive meals, hydration, and rest are practical supports.";
            case FOLLICULAR -> "You are in the follicular phase. This can be a good time to notice rising energy and compare mood or exercise logs.";
            case OVULATION -> "You are around ovulation. Hydration, balanced meals, and symptom tracking can make mid-cycle patterns easier to spot.";
            case LUTEAL -> "You are in the luteal phase. Watch sleep, stress, cravings, headaches, and PMS symptoms before the next expected period.";
        };
    }

    private boolean hasPremenstrualHeadaches(List<Symptom> symptoms, List<Cycle> cycles) {
        List<LocalDate> starts = cycles.stream().map(Cycle::getLastPeriodStartDate).toList();
        return symptoms.stream()
                .filter(symptom -> symptom.getType() == SymptomType.HEADACHE)
                .filter(symptom -> starts.stream().anyMatch(start -> !symptom.getEntryDate().isBefore(start.minusDays(3))
                        && symptom.getEntryDate().isBefore(start)))
                .count() >= 2;
    }

    private boolean hasProgressivelyHeavierFlow(List<FlowEntry> flowEntries) {
        if (flowEntries.size() < 6) {
            return false;
        }
        List<FlowEntry> chronological = chronological(flowEntries, FlowEntry::getEntryDate);
        int midpoint = chronological.size() / 2;
        double earlier = chronological.subList(0, midpoint).stream()
                .mapToInt(entry -> entry.getFlowLevel().ordinal())
                .average()
                .orElse(0);
        double later = chronological.subList(midpoint, chronological.size()).stream()
                .mapToInt(entry -> entry.getFlowLevel().ordinal())
                .average()
                .orElse(0);
        return later - earlier >= 1;
    }

    private boolean isHeavy(FlowEntry entry) {
        return entry.getFlowLevel() == FlowLevel.HEAVY || entry.getFlowLevel() == FlowLevel.VERY_HEAVY;
    }

    private double averageOnDates(List<SleepLog> sleepLogs, java.util.Set<LocalDate> dates) {
        return sleepLogs.stream()
                .filter(log -> dates.contains(log.getEntryDate()))
                .mapToDouble(SleepLog::getHours)
                .average()
                .orElse(0);
    }

    private double averageWaterOnDates(List<WaterLog> waterLogs, java.util.Set<LocalDate> dates) {
        return waterLogs.stream()
                .filter(log -> dates.contains(log.getEntryDate()))
                .collect(Collectors.groupingBy(WaterLog::getEntryDate, Collectors.summingInt(WaterLog::getAmountMl)))
                .values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    private <T> List<T> chronological(List<T> values, Function<T, LocalDate> dateExtractor) {
        return values.stream()
                .sorted(Comparator.comparing(dateExtractor))
                .toList();
    }

    private HealthIntelligenceCard card(String title, String message, InsightType type, String icon, String category) {
        return new HealthIntelligenceCard(title, message, type, icon, category);
    }

    private HealthIntelligenceCard card(com.cyclecare.domain.HealthInsight insight) {
        return new HealthIntelligenceCard(
                insight.getTitle(),
                insight.getMessage(),
                insight.getType(),
                insight.getType() == InsightType.ALERT ? "circle-alert" : "bell",
                "Health alert"
        );
    }

    private String rounded(double value) {
        return String.valueOf(Math.round(value));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
