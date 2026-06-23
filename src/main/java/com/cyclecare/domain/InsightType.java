package com.cyclecare.domain;

public enum InsightType {
    INFO("Info"),
    WARNING("Warning"),
    ALERT("Alert");

    private final String label;

    InsightType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
