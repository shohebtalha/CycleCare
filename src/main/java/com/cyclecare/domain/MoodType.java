package com.cyclecare.domain;

public enum MoodType {
    HAPPY("Happy"),
    NEUTRAL("Neutral"),
    SAD("Sad"),
    IRRITATED("Irritated"),
    ANXIOUS("Anxious"),
    ENERGETIC("Energetic"),
    CALM("Calm"),
    STRESSED("Stressed");

    private final String label;

    MoodType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
