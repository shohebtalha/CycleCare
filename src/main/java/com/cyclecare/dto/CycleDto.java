package com.cyclecare.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CycleDto {

    @NotNull(message = "Last period start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate lastPeriodStartDate;

    @NotNull(message = "Average cycle length is required")
    @Min(value = 15, message = "Cycle length must be at least 15 days")
    @Max(value = 60, message = "Cycle length must be 60 days or less")
    private Integer averageCycleLength = 28;

    @NotNull(message = "Average period duration is required")
    @Min(value = 1, message = "Period duration must be at least 1 day")
    @Max(value = 14, message = "Period duration must be 14 days or less")
    private Integer averagePeriodDuration = 5;

    @Size(max = 500, message = "Notes must be under 500 characters")
    private String notes;

    public LocalDate getLastPeriodStartDate() {
        return lastPeriodStartDate;
    }

    public void setLastPeriodStartDate(LocalDate lastPeriodStartDate) {
        this.lastPeriodStartDate = lastPeriodStartDate;
    }

    public Integer getAverageCycleLength() {
        return averageCycleLength;
    }

    public void setAverageCycleLength(Integer averageCycleLength) {
        this.averageCycleLength = averageCycleLength;
    }

    public Integer getAveragePeriodDuration() {
        return averagePeriodDuration;
    }

    public void setAveragePeriodDuration(Integer averagePeriodDuration) {
        this.averagePeriodDuration = averagePeriodDuration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
