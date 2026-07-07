package com.cyclecare.service;

public enum PredictionConfidence {
    HIGH("High Confidence"),
    MEDIUM("Medium Confidence"),
    LOW("Low Confidence");

    private final String label;

    PredictionConfidence(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
