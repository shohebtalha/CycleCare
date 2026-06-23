package com.cyclecare.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class WaterLogDto {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Water log date cannot be in the future")
    private LocalDate entryDate = LocalDate.now();

    @NotNull(message = "Amount is required")
    @Min(value = 50, message = "Amount must be at least 50 ml")
    @Max(value = 5000, message = "A single log must be 5000 ml or less")
    private Integer amountMl = 250;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public Integer getAmountMl() {
        return amountMl;
    }

    public void setAmountMl(Integer amountMl) {
        this.amountMl = amountMl;
    }
}
