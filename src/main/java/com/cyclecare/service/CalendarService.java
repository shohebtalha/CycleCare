package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.SymptomType;
import com.cyclecare.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final CycleService cycleService;
    private final SymptomService symptomService;

    public CalendarService(CycleService cycleService, SymptomService symptomService) {
        this.cycleService = cycleService;
        this.symptomService = symptomService;
    }

    @Transactional(readOnly = true)
    public List<CalendarDay> month(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Map<LocalDate, List<String>> symptomsByDate = symptomService.between(user, start, end).stream()
                .collect(Collectors.groupingBy(Symptom::getEntryDate, LinkedHashMap::new,
                        Collectors.mapping(this::symptomLabel, Collectors.toList())));

        Cycle cycle = cycleService.latestCycle(user).orElse(null);
        List<CalendarDay> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            CalendarDay day = new CalendarDay(date);
            day.getSymptoms().addAll(symptomsByDate.getOrDefault(date, List.of()));
            if (cycle != null) {
                applyCycleMarkers(day, date, today, cycle);
            }
            days.add(day);
        }
        return days;
    }

    private void applyCycleMarkers(CalendarDay day, LocalDate date, LocalDate today, Cycle cycle) {
        int cycleLength = Math.max(15, cycle.getAverageCycleLength());
        int periodDuration = Math.max(1, cycle.getAveragePeriodDuration());
        long daysSinceStart = ChronoUnit.DAYS.between(cycle.getLastPeriodStartDate(), date);
        long cycleIndex = Math.floorDiv(daysSinceStart, cycleLength);
        LocalDate cycleStart = cycle.getLastPeriodStartDate().plusDays(cycleIndex * cycleLength);
        int cycleDayIndex = (int) ChronoUnit.DAYS.between(cycleStart, date);
        LocalDate ovulationDate = cycleStart.plusDays(cycleLength - 14L);

        if (!date.isBefore(cycle.getLastPeriodStartDate()) && cycleDayIndex >= 0 && cycleDayIndex < periodDuration) {
            day.setPeriodDay(true);
            day.setPredictedPeriod(date.isAfter(today));
        }
        day.setOvulationDay(date.equals(ovulationDate));
        day.setFertilityWindow(!date.isBefore(ovulationDate.minusDays(5)) && !date.isAfter(ovulationDate.plusDays(1)));
    }

    private String symptomLabel(Symptom symptom) {
        if (symptom.getType() == SymptomType.OTHER && symptom.getCustomSymptom() != null
                && !symptom.getCustomSymptom().isBlank()) {
            return symptom.getCustomSymptom();
        }
        return symptom.getType().getLabel();
    }
}
