package com.cyclecare.service;

import java.util.List;

public record CycleStatistics(
        int averageCycleLength,
        int shortestCycleLength,
        int longestCycleLength,
        int mostRecentCycleLength,
        double standardDeviation,
        int variationDays,
        int regularityScore,
        int observedCycleCount,
        List<Integer> includedCycleLengths,
        List<Integer> outlierCycleLengths
) {
}
