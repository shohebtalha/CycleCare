package com.cyclecare.dto;

import com.cyclecare.domain.MoodType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class MoodDto {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Mood date cannot be in the future")
    private LocalDate entryDate = LocalDate.now();

    @NotNull(message = "Mood is required")
    private MoodType type;

    @NotNull(message = "Intensity is required")
    @Min(value = 1, message = "Intensity starts at 1")
    @Max(value = 5, message = "Intensity ends at 5")
    private Integer intensity = 3;

    @Size(max = 500, message = "Notes must be under 500 characters")
    private String notes;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public MoodType getType() {
        return type;
    }

    public void setType(MoodType type) {
        this.type = type;
    }

    public Integer getIntensity() {
        return intensity;
    }

    public void setIntensity(Integer intensity) {
        this.intensity = intensity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
