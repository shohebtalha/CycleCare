package com.cyclecare.domain;

public enum FlowColor {
    BRIGHT_RED("Bright Red"),
    DARK_RED("Dark Red"),
    BROWN("Brown"),
    PINK("Pink");

    private final String label;

    FlowColor(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
