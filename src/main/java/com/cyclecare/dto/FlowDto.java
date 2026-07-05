package com.cyclecare.dto;

import com.cyclecare.domain.ClotSize;
import com.cyclecare.domain.FlowColor;
import com.cyclecare.domain.FlowLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class FlowDto {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Flow date cannot be in the future")
    private LocalDate entryDate = LocalDate.now();

    @NotNull(message = "Flow level is required")
    private FlowLevel flowLevel;

    @NotNull(message = "Flow color is required")
    private FlowColor flowColor;

    @NotNull(message = "Clot size is required")
    private ClotSize clotSize = ClotSize.NONE;

    @Size(max = 500, message = "Notes must be under 500 characters")
    private String notes;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public FlowLevel getFlowLevel() {
        return flowLevel;
    }

    public void setFlowLevel(FlowLevel flowLevel) {
        this.flowLevel = flowLevel;
    }

    public FlowColor getFlowColor() {
        return flowColor;
    }

    public void setFlowColor(FlowColor flowColor) {
        this.flowColor = flowColor;
    }

    public ClotSize getClotSize() {
        return clotSize;
    }

    public void setClotSize(ClotSize clotSize) {
        this.clotSize = clotSize;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
