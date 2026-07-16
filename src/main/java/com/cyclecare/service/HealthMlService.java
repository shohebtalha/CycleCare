package com.cyclecare.service;

import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.FlowLevel;
import com.cyclecare.domain.CyclePredictionHistory;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.MoodType;
import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.SymptomType;
import com.cyclecare.domain.WaterLog;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HealthMlService {

    private static final int HYDRATION_TARGET_ML = 2000;

    public HealthMlPrediction predict(List<FlowEntry> flowEntries,
                                      List<Symptom> symptoms,
                                      List<Mood> moods,
                                      List<SleepLog> sleepLogs,
                                      List<WaterLog> waterLogs,
                                      int predictionConfidence,
                                      List<CyclePredictionHistory> predictionHistory) {
        List<ModelDriver> drivers = new ArrayList<>();

        double severeSymptomRate = rate(symptoms.stream()
                .filter(symptom -> symptom.getType() != SymptomType.NO_SYMPTOM)
                .filter(symptom -> symptom.getSeverity() != null && symptom.getSeverity() >= 4)
                .count(), symptoms.size());
        addDriver(drivers, "Severe symptom pattern", severeSymptomRate * 34,
                "Severe symptoms are appearing in recent logs.");

        double heavyFlowRate = rate(flowEntries.stream().filter(this::isHeavy).count(), flowEntries.size());
        addDriver(drivers, "Heavy flow signal", heavyFlowRate * 24,
                "Heavy flow days are increasing the model score.");

        double lowSleepRate = rate(sleepLogs.stream()
                .filter(log -> log.getHours() != null && log.getHours() < 6)
                .count(), sleepLogs.size());
        addDriver(drivers, "Low sleep recovery", lowSleepRate * 18,
                "Short sleep appears often enough to affect recovery.");

        double lowHydrationRate = lowHydrationRate(waterLogs);
        addDriver(drivers, "Hydration gap", lowHydrationRate * 14,
                "Logged hydration is below the target on multiple days.");

        double stressMoodRate = rate(moods.stream().filter(this::isStressLike).count(), moods.size());
        addDriver(drivers, "Stress mood cluster", stressMoodRate * 12,
                "Stress-like moods are repeating in the recent window.");

        double predictionMissRate = rate(predictionHistory.stream()
                .filter(history -> history.getPredictionErrorDays() != null)
                .filter(history -> Math.abs(history.getPredictionErrorDays()) >= 3)
                .count(), predictionHistory.size());
        addDriver(drivers, "Prediction mismatch", predictionMissRate * 14,
                "Recent predicted dates differed from confirmed period starts.");

        int dataVolume = flowEntries.size() + symptoms.size() + moods.size() + sleepLogs.size() + waterLogs.size();
        double rawRisk = drivers.stream().mapToDouble(ModelDriver::score).sum();
        int riskScore = clamp((int) Math.round(rawRisk));
        int modelConfidence = clamp((predictionConfidence + Math.min(100, dataVolume * 2)) / 2);

        drivers.sort(Comparator.comparingDouble(ModelDriver::score).reversed());
        List<String> driverLabels = drivers.stream()
                .filter(driver -> driver.score() >= 4)
                .limit(4)
                .map(ModelDriver::label)
                .toList();

        String primaryDriver = drivers.stream()
                .filter(driver -> driver.score() >= 4)
                .findFirst()
                .map(ModelDriver::message)
                .orElse("No strong risk driver detected yet. More logs will make the model more personal.");

        return new HealthMlPrediction(
                riskScore,
                riskLevel(riskScore),
                modelConfidence,
                primaryDriver,
                nextBestAction(drivers),
                driverLabels.isEmpty() ? List.of("Baseline learning") : driverLabels
        );
    }

    private double lowHydrationRate(List<WaterLog> waterLogs) {
        Map<LocalDate, Integer> totalsByDate = waterLogs.stream()
                .collect(Collectors.groupingBy(WaterLog::getEntryDate, Collectors.summingInt(WaterLog::getAmountMl)));
        return rate(totalsByDate.values().stream()
                .filter(total -> total < HYDRATION_TARGET_ML)
                .count(), totalsByDate.size());
    }

    private String riskLevel(int riskScore) {
        if (riskScore >= 60) {
            return "High attention";
        }
        if (riskScore >= 35) {
            return "Watch closely";
        }
        return "Stable";
    }

    private String nextBestAction(List<ModelDriver> drivers) {
        return drivers.stream()
                .filter(driver -> driver.score() >= 4)
                .findFirst()
                .map(driver -> switch (driver.label()) {
                    case "Severe symptom pattern" -> "Prioritize symptom severity logging and consider clinical guidance if severe symptoms continue.";
                    case "Heavy flow signal" -> "Track flow and fatigue together for the next cycle so the model can separate normal variation from escalation.";
                    case "Low sleep recovery" -> "Protect sleep during the next predicted period window and compare symptom intensity afterward.";
                    case "Hydration gap" -> "Aim for steadier daily hydration logs before and during bleeding days.";
                    case "Stress mood cluster" -> "Add short journal notes on stressful days so mood triggers become easier to classify.";
                    case "Prediction mismatch" -> "Keep confirming actual period start dates so prediction errors keep shrinking.";
                    default -> "Keep logging cycle, symptom, mood, water, and sleep data together.";
                })
                .orElse("Keep logging cycle, symptom, mood, water, and sleep data together.");
    }

    private void addDriver(List<ModelDriver> drivers, String label, double score, String message) {
        drivers.add(new ModelDriver(label, score, message));
    }

    private double rate(long count, int total) {
        if (total == 0) {
            return 0;
        }
        return (double) count / total;
    }

    private boolean isHeavy(FlowEntry entry) {
        return entry.getFlowLevel() == FlowLevel.HEAVY || entry.getFlowLevel() == FlowLevel.VERY_HEAVY;
    }

    private boolean isStressLike(Mood mood) {
        return mood.getType() == MoodType.STRESSED
                || mood.getType() == MoodType.ANXIOUS
                || mood.getType() == MoodType.IRRITATED
                || mood.getType() == MoodType.SAD;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private record ModelDriver(String label, double score, String message) {
    }
}
