package com.cyclecare.domain;

public enum SleepQuality {
    POOR("Poor"),
    FAIR("Fair"),
    GOOD("Good"),
    EXCELLENT("Excellent");

    private final String label;

    SleepQuality(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
