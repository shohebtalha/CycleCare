package com.cyclecare.domain;

public enum FlowLevel {
    SPOTTING("Spotting"),
    LIGHT("Light"),
    MODERATE("Moderate"),
    HEAVY("Heavy"),
    VERY_HEAVY("Very Heavy");

    private final String label;

    FlowLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
