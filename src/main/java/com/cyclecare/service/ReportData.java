package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.JournalEntry;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.User;
import com.cyclecare.domain.WaterLog;

import java.time.LocalDate;
import java.util.List;

public record ReportData(
        User user,
        ReportRange range,
        LocalDate startDate,
        LocalDate endDate,
        List<Cycle> cycles,
        List<Symptom> symptoms,
        List<Mood> moods,
        List<WaterLog> waterLogs,
        List<SleepLog> sleepLogs,
        List<FlowEntry> flowEntries,
        List<JournalEntry> journalEntries
) {
}
