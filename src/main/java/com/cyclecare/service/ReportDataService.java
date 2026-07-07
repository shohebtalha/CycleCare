package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportDataService {

    private final CycleService cycleService;
    private final SymptomService symptomService;
    private final MoodService moodService;
    private final WaterService waterService;
    private final SleepService sleepService;
    private final JournalService journalService;
    private final FlowService flowService;

    public ReportDataService(CycleService cycleService,
                             SymptomService symptomService,
                             MoodService moodService,
                             WaterService waterService,
                             SleepService sleepService,
                             JournalService journalService,
                             FlowService flowService) {
        this.cycleService = cycleService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.journalService = journalService;
        this.flowService = flowService;
    }

    @Transactional(readOnly = true)
    public ReportData collect(User user, ReportRange range) {
        LocalDate end = LocalDate.now();
        LocalDate start = range.startDate(end);
        List<Cycle> cycles = cycleService.allCycles(user).stream()
                .filter(cycle -> !cycle.getLastPeriodStartDate().isBefore(start)
                        && !cycle.getLastPeriodStartDate().isAfter(end))
                .toList();

        return new ReportData(
                user,
                range,
                start,
                end,
                cycles,
                symptomService.between(user, start, end),
                moodService.between(user, start, end),
                waterService.between(user, start, end),
                sleepService.between(user, start, end),
                flowService.between(user, start, end),
                journalService.between(user, start, end)
        );
    }
}
