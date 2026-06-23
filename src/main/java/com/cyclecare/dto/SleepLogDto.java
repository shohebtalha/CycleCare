package com.cyclecare.dto;

import com.cyclecare.domain.SleepQuality;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SleepLogDto {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Sleep log date cannot be in the future")
    private LocalDate entryDate = LocalDate.now();

    @NotNull(message = "Sleep hours are required")
    @DecimalMin(value = "0.5", message = "Sleep hours must be at least 0.5")
    @DecimalMax(value = "18.0", message = "Sleep hours must be 18 or less")
    private Double hours = 7.0;

    @NotNull(message = "Sleep quality is required")
    private SleepQuality quality = SleepQuality.GOOD;

    @Size(max = 500, message = "Notes must be under 500 characters")
    private String notes;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
    }

    public SleepQuality getQuality() {
        return quality;
    }

    public void setQuality(SleepQuality quality) {
        this.quality = quality;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
