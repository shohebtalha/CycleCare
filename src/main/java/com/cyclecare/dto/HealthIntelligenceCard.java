package com.cyclecare.dto;

import com.cyclecare.domain.InsightType;

public record HealthIntelligenceCard(
        String title,
        String message,
        InsightType type,
        String icon,
        String category
) {
}
