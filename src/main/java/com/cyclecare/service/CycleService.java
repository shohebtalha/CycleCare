package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.CyclePredictionHistory;
import com.cyclecare.domain.User;
import com.cyclecare.dto.CycleDto;
import com.cyclecare.repository.CyclePredictionHistoryRepository;
import com.cyclecare.repository.CycleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CycleService {

    private final CycleRepository cycleRepository;
    private final AdaptiveCyclePredictionService adaptiveCyclePredictionService;
    private final CyclePredictionHistoryRepository predictionHistoryRepository;

    public CycleService(CycleRepository cycleRepository,
                        AdaptiveCyclePredictionService adaptiveCyclePredictionService,
                        CyclePredictionHistoryRepository predictionHistoryRepository) {
        this.cycleRepository = cycleRepository;
        this.adaptiveCyclePredictionService = adaptiveCyclePredictionService;
        this.predictionHistoryRepository = predictionHistoryRepository;
    }

    @Transactional
    public Cycle saveCycle(User user, CycleDto dto) {
        List<Cycle> previousCycles = allCycles(user);
        Optional<CyclePrediction> previousPrediction = adaptiveCyclePredictionService.predict(previousCycles, dto.getLastPeriodStartDate());

        Cycle cycle = new Cycle();
        cycle.setUser(user);
        cycle.setLastPeriodStartDate(dto.getLastPeriodStartDate());
        cycle.setAverageCycleLength(dto.getAverageCycleLength());
        cycle.setAveragePeriodDuration(dto.getAveragePeriodDuration());
        cycle.setNotes(dto.getNotes());

        previousCycles.stream()
                .filter(existing -> existing.getLastPeriodStartDate().isBefore(dto.getLastPeriodStartDate()))
                .findFirst()
                .ifPresent(previous -> cycle.setActualCycleLength((int) ChronoUnit.DAYS.between(
                        previous.getLastPeriodStartDate(), dto.getLastPeriodStartDate())));

        Cycle saved = cycleRepository.save(cycle);
        recordPredictionOutcome(user, previousPrediction, saved);
        return saved;
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
        return adaptiveCyclePredictionService.predict(allCycles(user), LocalDate.now());
    }

    public Optional<CyclePrediction> currentPrediction(List<Cycle> recentCycles) {
        return adaptiveCyclePredictionService.predict(recentCycles, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public CycleStatistics statistics(User user) {
        return adaptiveCyclePredictionService.statistics(allCycles(user));
    }

    @Transactional(readOnly = true)
    public List<CyclePredictionHistory> predictionHistory(User user) {
        return predictionHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    private void recordPredictionOutcome(User user, Optional<CyclePrediction> previousPrediction, Cycle actualCycle) {
        if (actualCycle.getActualCycleLength() == null || previousPrediction.isEmpty()
                || predictionHistoryRepository.existsByUserAndActualPeriodStartDate(user, actualCycle.getLastPeriodStartDate())) {
            return;
        }

        CyclePrediction prediction = previousPrediction.get();
        int errorDays = (int) ChronoUnit.DAYS.between(prediction.getNextPeriodDate(), actualCycle.getLastPeriodStartDate());

        CyclePredictionHistory history = new CyclePredictionHistory();
        history.setUser(user);
        history.setPredictedPeriodDate(prediction.getNextPeriodDate());
        history.setActualPeriodStartDate(actualCycle.getLastPeriodStartDate());
        history.setPredictedCycleLength(prediction.getStatistics() != null
                ? prediction.getStatistics().averageCycleLength()
                : actualCycle.getAverageCycleLength());
        history.setActualCycleLength(actualCycle.getActualCycleLength());
        history.setPredictionErrorDays(errorDays);
        history.setConfidence(prediction.getConfidence().name());
        history.setExplanation(prediction.getExplanation());
        predictionHistoryRepository.save(history);
    }
}
