package com.cyclecare.service;

import com.cyclecare.dto.HealthIntelligenceCard;

import java.util.List;

public record MlIntelligenceResult(
        List<HealthIntelligenceCard> cards,
        HealthMlPrediction prediction,
        boolean available
) {
    public static MlIntelligenceResult unavailable() {
        return new MlIntelligenceResult(List.of(), null, false);
    }
}
