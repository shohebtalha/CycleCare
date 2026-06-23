package com.cyclecare.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {

    private final LocalDate date;
    private boolean periodDay;
    private boolean predictedPeriod;
    private boolean ovulationDay;
    private boolean fertilityWindow;
    private final List<String> symptoms = new ArrayList<>();

    public CalendarDay(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isPeriodDay() {
        return periodDay;
    }

    public void setPeriodDay(boolean periodDay) {
        this.periodDay = periodDay;
    }

    public boolean isPredictedPeriod() {
        return predictedPeriod;
    }

    public void setPredictedPeriod(boolean predictedPeriod) {
        this.predictedPeriod = predictedPeriod;
    }

    public boolean isOvulationDay() {
        return ovulationDay;
    }

    public void setOvulationDay(boolean ovulationDay) {
        this.ovulationDay = ovulationDay;
    }

    public boolean isFertilityWindow() {
        return fertilityWindow;
    }

    public void setFertilityWindow(boolean fertilityWindow) {
        this.fertilityWindow = fertilityWindow;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }
}
