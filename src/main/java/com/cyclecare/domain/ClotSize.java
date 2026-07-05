package com.cyclecare.domain;

public enum ClotSize {
    NONE("None"),
    SMALL("Small"),
    LARGE("Large");

    private final String label;

    ClotSize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
