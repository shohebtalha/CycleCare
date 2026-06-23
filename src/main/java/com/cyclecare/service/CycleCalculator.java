package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.MenstrualPhase;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class CycleCalculator {

    public CyclePrediction calculate(Cycle cycle, LocalDate today) {
        int cycleLength = Math.max(15, cycle.getAverageCycleLength());
        int periodDuration = Math.max(1, cycle.getAveragePeriodDuration());
        LocalDate firstStart = cycle.getLastPeriodStartDate();

        long daysSinceFirstStart = ChronoUnit.DAYS.between(firstStart, today);
        if (daysSinceFirstStart < 0) {
            daysSinceFirstStart = 0;
        }

        long completedCycles = daysSinceFirstStart / cycleLength;
        LocalDate currentCycleStart = firstStart.plusDays(completedCycles * cycleLength);
        int currentCycleDay = (int) ChronoUnit.DAYS.between(currentCycleStart, today) + 1;

        LocalDate nextPeriodDate = currentCycleStart.plusDays(cycleLength);
        LocalDate ovulationDate = nextPeriodDate.minusDays(14);
        LocalDate fertilityWindowStart = ovulationDate.minusDays(5);
        LocalDate fertilityWindowEnd = ovulationDate.plusDays(1);
        LocalDate upcomingOvulation = ovulationDate.isBefore(today)
                ? ovulationDate.plusDays(cycleLength)
                : ovulationDate;

        MenstrualPhase phase = determinePhase(today, currentCycleDay, periodDuration, ovulationDate);

        return new CyclePrediction(
                currentCycleStart,
                currentCycleDay,
                nextPeriodDate,
                ovulationDate,
                fertilityWindowStart,
                fertilityWindowEnd,
                phase,
                Math.max(0, ChronoUnit.DAYS.between(today, nextPeriodDate)),
                Math.max(0, ChronoUnit.DAYS.between(today, upcomingOvulation))
        );
    }

    private MenstrualPhase determinePhase(LocalDate today,
                                          int currentCycleDay,
                                          int periodDuration,
                                          LocalDate ovulationDate) {
        if (currentCycleDay <= periodDuration) {
            return MenstrualPhase.MENSTRUAL;
        }
        if (!today.isBefore(ovulationDate.minusDays(1)) && !today.isAfter(ovulationDate.plusDays(1))) {
            return MenstrualPhase.OVULATION;
        }
        if (today.isBefore(ovulationDate)) {
            return MenstrualPhase.FOLLICULAR;
        }
        return MenstrualPhase.LUTEAL;
    }
}
