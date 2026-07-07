package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.MenstrualPhase;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AdaptiveCyclePredictionService {

    private static final int MIN_REASONABLE_CYCLE = 15;
    private static final int MAX_REASONABLE_CYCLE = 90;
    private static final int OVULATION_OFFSET_DAYS = 14;

    public Optional<CyclePrediction> predict(List<Cycle> cycles, LocalDate today) {
        if (cycles.isEmpty()) {
            return Optional.empty();
        }

        List<Cycle> chronological = cycles.stream()
                .sorted(Comparator.comparing(Cycle::getLastPeriodStartDate))
                .toList();
        Cycle latest = chronological.get(chronological.size() - 1);
        CycleStatistics statistics = statistics(chronological);
        int predictedCycleLength = statistics.averageCycleLength();
        int periodDuration = Math.max(1, latest.getAveragePeriodDuration());

        LocalDate currentCycleStart = latest.getLastPeriodStartDate();
        int currentCycleDay = (int) ChronoUnit.DAYS.between(currentCycleStart, today) + 1;
        if (currentCycleDay < 1) {
            currentCycleDay = 1;
        }

        LocalDate nextPeriodDate = currentCycleStart.plusDays(predictedCycleLength);
        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(nextPeriodDate, today));
        LocalDate ovulationDate = nextPeriodDate.minusDays(OVULATION_OFFSET_DAYS);
        LocalDate fertilityWindowStart = ovulationDate.minusDays(5);
        LocalDate fertilityWindowEnd = ovulationDate.plusDays(1);
        LocalDate nextOvulation = ovulationDate.isBefore(today)
                ? ovulationDate.plusDays(predictedCycleLength)
                : ovulationDate;

        PredictionConfidence confidence = confidence(statistics);
        MenstrualPhase phase = overdueDays > 0
                ? MenstrualPhase.LUTEAL
                : determinePhase(today, currentCycleDay, periodDuration, ovulationDate);

        return Optional.of(new CyclePrediction(
                currentCycleStart,
                currentCycleDay,
                nextPeriodDate,
                ovulationDate,
                fertilityWindowStart,
                fertilityWindowEnd,
                phase,
                Math.max(0, ChronoUnit.DAYS.between(today, nextPeriodDate)),
                Math.max(0, ChronoUnit.DAYS.between(today, nextOvulation)),
                overdueDays,
                confidence,
                statistics,
                explanation(statistics)
        ));
    }

    public CycleStatistics statistics(List<Cycle> cycles) {
        List<Cycle> chronological = cycles.stream()
                .sorted(Comparator.comparing(Cycle::getLastPeriodStartDate))
                .toList();
        List<Integer> observedLengths = observedLengths(chronological);
        if (observedLengths.isEmpty()) {
            if (chronological.isEmpty()) {
                return new CycleStatistics(0, 0, 0, 0, 0, 0, 0, 0, List.of(), List.of());
            }
            int fallback = chronological.stream()
                    .reduce((first, second) -> second)
                    .map(Cycle::getAverageCycleLength)
                    .map(length -> Math.max(MIN_REASONABLE_CYCLE, length))
                    .orElse(0);
            return new CycleStatistics(fallback, fallback, fallback, fallback, 0, 0, 55, 0,
                    List.of(fallback), List.of());
        }

        double median = median(observedLengths);
        double rawStdDev = standardDeviation(observedLengths, observedLengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(median));
        double outlierThreshold = Math.max(7, rawStdDev * 2);
        List<Integer> included = new ArrayList<>();
        List<Integer> outliers = new ArrayList<>();
        for (Integer length : observedLengths) {
            boolean unreasonable = length < MIN_REASONABLE_CYCLE || length > MAX_REASONABLE_CYCLE;
            boolean statisticalOutlier = observedLengths.size() >= 4 && Math.abs(length - median) > outlierThreshold;
            if (unreasonable || statisticalOutlier) {
                outliers.add(length);
            } else {
                included.add(length);
            }
        }
        if (included.isEmpty()) {
            included.addAll(observedLengths);
        }

        int weightedAverage = weightedAverage(observedLengths, outliers);
        int shortest = included.stream().mapToInt(Integer::intValue).min().orElse(weightedAverage);
        int longest = included.stream().mapToInt(Integer::intValue).max().orElse(weightedAverage);
        int recent = observedLengths.get(observedLengths.size() - 1);
        double standardDeviation = standardDeviation(included, included.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(weightedAverage));
        int variation = longest - shortest;
        int regularityScore = Math.max(0, Math.min(100, (int) Math.round(100 - (standardDeviation * 9))));

        return new CycleStatistics(weightedAverage, shortest, longest, recent, standardDeviation, variation,
                regularityScore, observedLengths.size(), List.copyOf(included), List.copyOf(outliers));
    }

    private List<Integer> observedLengths(List<Cycle> chronological) {
        List<Integer> lengths = new ArrayList<>();
        for (int index = 1; index < chronological.size(); index++) {
            long days = ChronoUnit.DAYS.between(
                    chronological.get(index - 1).getLastPeriodStartDate(),
                    chronological.get(index).getLastPeriodStartDate());
            if (days > 0) {
                lengths.add((int) days);
            }
        }
        return lengths;
    }

    private int weightedAverage(List<Integer> observedLengths, List<Integer> outliers) {
        double weightedTotal = 0;
        double totalWeight = 0;
        List<Integer> newestFirst = new ArrayList<>(observedLengths);
        Collections.reverse(newestFirst);
        for (int index = 0; index < newestFirst.size(); index++) {
            int length = newestFirst.get(index);
            double weight = Math.pow(0.82, index);
            if (outliers.contains(length)) {
                weight *= 0.2;
            }
            weightedTotal += length * weight;
            totalWeight += weight;
        }
        return (int) Math.round(weightedTotal / totalWeight);
    }

    private PredictionConfidence confidence(CycleStatistics statistics) {
        if (statistics.observedCycleCount() >= 5 && statistics.standardDeviation() <= 2.5
                && statistics.outlierCycleLengths().isEmpty()) {
            return PredictionConfidence.HIGH;
        }
        if (statistics.observedCycleCount() >= 3 && statistics.standardDeviation() <= 5.5) {
            return PredictionConfidence.MEDIUM;
        }
        return PredictionConfidence.LOW;
    }

    private String explanation(CycleStatistics statistics) {
        if (statistics.observedCycleCount() == 0) {
            return "This prediction uses your saved cycle length estimate until you log at least two period start dates.";
        }
        String base = "Prediction uses a weighted average of your confirmed cycle starts, with recent cycles weighted more heavily.";
        if (!statistics.outlierCycleLengths().isEmpty()) {
            return base + " Unusual cycle length(s) " + statistics.outlierCycleLengths()
                    + " were treated as low-weight outliers so one abnormal cycle does not dominate future predictions.";
        }
        if (statistics.includedCycleLengths().size() >= 5) {
            List<Integer> lengths = statistics.includedCycleLengths();
            double earlier = lengths.subList(0, lengths.size() / 2).stream().mapToInt(Integer::intValue).average().orElse(0);
            double recent = lengths.subList(lengths.size() / 2, lengths.size()).stream().mapToInt(Integer::intValue).average().orElse(0);
            int shift = (int) Math.round(recent - earlier);
            if (Math.abs(shift) >= 2) {
                return base + " Recent cycles are " + Math.abs(shift) + " day(s) "
                        + (shift > 0 ? "longer" : "shorter")
                        + ", so the next predicted period shifted accordingly.";
            }
        }
        return base + " Your cycle variation is " + statistics.variationDays()
                + " day(s), giving " + statistics.regularityScore() + "/100 regularity.";
    }

    private MenstrualPhase determinePhase(LocalDate today,
                                          int currentCycleDay,
                                          int periodDuration,
                                          LocalDate ovulationDate) {
        if (currentCycleDay <= periodDuration) {
            return MenstrualPhase.MENSTRUAL;
        }
        if (!today.isBefore(ovulationDate.minusDays(1)) && !today.isAfter(ovulationDate.plusDays(1))) {
            return MenstrualPhase.OVULATION;
        }
        if (today.isBefore(ovulationDate)) {
            return MenstrualPhase.FOLLICULAR;
        }
        return MenstrualPhase.LUTEAL;
    }

    private double median(List<Integer> values) {
        List<Integer> sorted = values.stream().sorted().toList();
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
        }
        return sorted.get(middle);
    }

    private double standardDeviation(List<Integer> values, double average) {
        if (values.size() < 2) {
            return 0;
        }
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - average, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }
}
