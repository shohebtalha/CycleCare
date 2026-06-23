package com.cyclecare.domain;

public enum ActivityLevel {
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High");

    private final String label;

    ActivityLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
