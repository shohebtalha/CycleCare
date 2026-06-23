package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.User;
import com.cyclecare.dto.CycleDto;
import com.cyclecare.repository.CycleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CycleService {

    private final CycleRepository cycleRepository;
    private final CycleCalculator cycleCalculator;

    public CycleService(CycleRepository cycleRepository, CycleCalculator cycleCalculator) {
        this.cycleRepository = cycleRepository;
        this.cycleCalculator = cycleCalculator;
    }

    @Transactional
    public Cycle saveCycle(User user, CycleDto dto) {
        Cycle cycle = new Cycle();
        cycle.setUser(user);
        cycle.setLastPeriodStartDate(dto.getLastPeriodStartDate());
        cycle.setAverageCycleLength(dto.getAverageCycleLength());
        cycle.setAveragePeriodDuration(dto.getAveragePeriodDuration());
        cycle.setNotes(dto.getNotes());
        return cycleRepository.save(cycle);
    }

    @Transactional(readOnly = true)
    public Optional<Cycle> latestCycle(User user) {
        return cycleRepository.findTopByUserOrderByLastPeriodStartDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Cycle> recentCycles(User user) {
        return cycleRepository.findTop12ByUserOrderByLastPeriodStartDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Cycle> allCycles(User user) {
        return cycleRepository.findByUserOrderByLastPeriodStartDateDesc(user);
    }

    @Transactional(readOnly = true)
    public Optional<CyclePrediction> currentPrediction(User user) {
        return latestCycle(user).map(cycle -> cycleCalculator.calculate(cycle, LocalDate.now()));
    }
}
