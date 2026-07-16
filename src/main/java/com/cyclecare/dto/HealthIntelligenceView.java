package com.cyclecare.dto;

import java.util.List;

public record HealthIntelligenceView(
        int healthScore,
        int predictionConfidence,
        int adaptiveRiskScore,
        String riskLevel,
        int modelConfidence,
        String modelPrimaryDriver,
        String modelNextBestAction,
        List<String> modelDrivers,
        String cyclePattern,
        String phaseSummary,
        List<HealthIntelligenceCard> alerts,
        List<HealthIntelligenceCard> trends,
        List<HealthIntelligenceCard> correlations,
        List<HealthIntelligenceCard> recommendations
) {
}
