package com.cyclecare.service;

import java.util.List;

public record HealthMlPrediction(
        int adaptiveRiskScore,
        String riskLevel,
        int modelConfidence,
        String primaryDriver,
        String nextBestAction,
        List<String> drivers
) {
}
