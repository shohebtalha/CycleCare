package com.cyclecare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class JournalEntryDto {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Journal date cannot be in the future")
    private LocalDate entryDate = LocalDate.now();

    @Size(max = 1200, message = "Physical symptoms must be under 1200 characters")
    private String physicalSymptoms;

    @Size(max = 1200, message = "Emotional state must be under 1200 characters")
    private String emotionalState;

    @Size(max = 3000, message = "Observations must be under 3000 characters")
    private String observations;

    @Size(max = 3000)
    private String nutritionLog;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getPhysicalSymptoms() {
        return physicalSymptoms;
    }

    public void setPhysicalSymptoms(String physicalSymptoms) {
        this.physicalSymptoms = physicalSymptoms;
    }

    public String getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    public String getNutritionLog() {
        return nutritionLog;
    }

    public void setNutritionLog(String nutritionLog) {
        this.nutritionLog = nutritionLog;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
