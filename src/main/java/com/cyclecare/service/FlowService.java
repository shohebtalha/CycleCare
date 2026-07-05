package com.cyclecare.service;

import com.cyclecare.domain.ClotSize;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.FlowLevel;
import com.cyclecare.domain.User;
import com.cyclecare.dto.FlowDto;
import com.cyclecare.repository.FlowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlowService {

    private final FlowRepository flowRepository;

    public FlowService(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
    }

    @Transactional
    public FlowEntry save(User user, FlowDto dto) {
        if (flowRepository.existsByUserAndEntryDate(user, dto.getEntryDate())) {
            throw new IllegalArgumentException("Blood flow is already logged for this date.");
        }
        FlowEntry entry = new FlowEntry();
        entry.setUser(user);
        entry.setEntryDate(dto.getEntryDate());
        entry.setFlowLevel(dto.getFlowLevel());
        entry.setFlowColor(dto.getFlowColor());
        entry.setClotSize(dto.getClotSize());
        entry.setNotes(dto.getNotes());
        return flowRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<FlowEntry> history(User user) {
        return flowRepository.findByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<FlowEntry> between(User user, LocalDate start, LocalDate end) {
        return flowRepository.findByUserAndEntryDateBetweenOrderByEntryDateDesc(user, start, end);
    }

    @Transactional(readOnly = true)
    public Optional<FlowEntry> today(User user) {
        return flowRepository.findByUserAndEntryDate(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> distribution(User user) {
        Map<String, Long> counts = history(user).stream()
                .collect(Collectors.groupingBy(entry -> entry.getFlowLevel().getLabel(), Collectors.counting()));
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (FlowLevel level : FlowLevel.values()) {
            distribution.put(level.getLabel(), counts.getOrDefault(level.getLabel(), 0L));
        }
        return distribution;
    }

    @Transactional(readOnly = true)
    public Optional<FlowLevel> mostCommonLevel(User user) {
        return history(user).stream()
                .collect(Collectors.groupingBy(FlowEntry::getFlowLevel, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.<FlowLevel, Long>comparingByValue()
                        .thenComparing(entry -> entry.getKey().ordinal()))
                .map(Map.Entry::getKey);
    }

    public Optional<FlowLevel> mostCommonLevel(List<FlowEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(FlowEntry::getFlowLevel, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.<FlowLevel, Long>comparingByValue()
                        .thenComparing(entry -> entry.getKey().ordinal()))
                .map(Map.Entry::getKey);
    }

    public String summary(List<FlowEntry> entries) {
        if (entries.isEmpty()) {
            return "No blood flow entries recorded for this period.";
        }
        List<FlowEntry> chronological = entries.stream()
                .sorted(Comparator.comparing(FlowEntry::getEntryDate))
                .toList();
        FlowLevel first = chronological.get(0).getFlowLevel();
        FlowLevel last = chronological.get(chronological.size() - 1).getFlowLevel();
        long heavyDays = entries.stream()
                .filter(entry -> entry.getFlowLevel() == FlowLevel.HEAVY || entry.getFlowLevel() == FlowLevel.VERY_HEAVY)
                .count();
        String base = heavyDays > 0
                ? "Heavy flow occurred on " + heavyDays + " day(s), with the pattern moving from " + first.getLabel() + " to " + last.getLabel() + "."
                : "Flow pattern moved from " + first.getLabel() + " to " + last.getLabel() + ".";
        return base + " Overall pattern appears consistent. If heavy flow persists for several consecutive days, consider consulting a healthcare professional.";
    }

    @Transactional(readOnly = true)
    public boolean hasRepeatedLargeClots(User user) {
        return history(user).stream()
                .filter(entry -> entry.getClotSize() == ClotSize.LARGE)
                .limit(30)
                .count() >= 3;
    }
}
