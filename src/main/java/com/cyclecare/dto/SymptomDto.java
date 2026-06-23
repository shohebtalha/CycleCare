package com.cyclecare.dto;

import com.cyclecare.domain.SymptomType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SymptomDto {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Symptom date cannot be in the future")
    private LocalDate entryDate = LocalDate.now();

    @NotNull(message = "Symptom is required")
    private SymptomType type;

    @Size(max = 120, message = "Custom symptom must be under 120 characters")
    private String customSymptom;

    @NotNull(message = "Severity is required")
    @Min(value = 1, message = "Severity starts at 1")
    @Max(value = 5, message = "Severity ends at 5")
    private Integer severity = 2;

    @Size(max = 500, message = "Notes must be under 500 characters")
    private String notes;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public SymptomType getType() {
        return type;
    }

    public void setType(SymptomType type) {
        this.type = type;
    }

    public String getCustomSymptom() {
        return customSymptom;
    }

    public void setCustomSymptom(String customSymptom) {
        this.customSymptom = customSymptom;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
