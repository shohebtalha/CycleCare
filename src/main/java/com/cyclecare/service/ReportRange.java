package com.cyclecare.service;

import java.time.LocalDate;

public enum ReportRange {
    TODAY("today", "Today", 0),
    PAST_WEEK("past_week", "Past Week", 6),
    PAST_MONTH("past_month", "Past Month", 29),
    THIS_MONTH("this_month", "This Month", -1);

    private final String requestValue;
    private final String label;
    private final int daysBack;

    ReportRange(String requestValue, String label, int daysBack) {
        this.requestValue = requestValue;
        this.label = label;
        this.daysBack = daysBack;
    }

    public String getRequestValue() {
        return requestValue;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate startDate(LocalDate endDate) {
        if (this == THIS_MONTH) {
            return endDate.withDayOfMonth(1);
        }
        return endDate.minusDays(daysBack);
    }

    public static ReportRange fromRequest(String value) {
        for (ReportRange range : values()) {
            if (range.requestValue.equalsIgnoreCase(value) || range.name().equalsIgnoreCase(value)) {
                return range;
            }
        }
        return PAST_MONTH;
    }
}
