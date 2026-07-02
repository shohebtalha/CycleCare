package com.cyclecare.domain;

public enum SymptomType {
    CRAMPS("Cramps"),
    HEADACHE("Headache"),
    ACNE("Acne"),
    FATIGUE("Fatigue"),
    BLOATING("Bloating"),
    BACK_PAIN("Back pain"),
    BREAST_TENDERNESS("Breast tenderness"),
    NAUSEA("Nausea"),
    NO_SYMPTOM("No Symptom"),
    OTHER("Other");

    private final String label;

    SymptomType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
