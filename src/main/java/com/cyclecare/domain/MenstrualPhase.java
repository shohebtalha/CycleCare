package com.cyclecare.domain;

public enum MenstrualPhase {
    MENSTRUAL("Menstrual"),
    FOLLICULAR("Follicular"),
    OVULATION("Ovulation"),
    LUTEAL("Luteal");

    private final String label;

    MenstrualPhase(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
