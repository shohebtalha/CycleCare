package com.cyclecare.dto;

import java.util.List;

public record HealthIntelligenceView(
        int healthScore,
        int predictionConfidence,
        String cyclePattern,
        String phaseSummary,
        List<HealthIntelligenceCard> alerts,
        List<HealthIntelligenceCard> trends,
        List<HealthIntelligenceCard> correlations,
        List<HealthIntelligenceCard> recommendations
) {
}
