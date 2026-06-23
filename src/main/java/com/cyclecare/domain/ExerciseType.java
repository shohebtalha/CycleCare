package com.cyclecare.domain;

public enum ExerciseType {
    WALKING("Walking"),
    YOGA("Yoga"),
    STRETCHING("Stretching"),
    STRENGTH_TRAINING("Strength training"),
    CARDIO("Cardio");

    private final String label;

    ExerciseType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
