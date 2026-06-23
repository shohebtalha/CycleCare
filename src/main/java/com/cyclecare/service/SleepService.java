package com.cyclecare.service;

import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.User;
import com.cyclecare.dto.SleepLogDto;
import com.cyclecare.repository.SleepLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SleepService {

    private final SleepLogRepository sleepLogRepository;

    public SleepService(SleepLogRepository sleepLogRepository) {
        this.sleepLogRepository = sleepLogRepository;
    }

    @Transactional
    public SleepLog save(User user, SleepLogDto dto) {
        SleepLog log = new SleepLog();
        log.setUser(user);
        log.setEntryDate(dto.getEntryDate());
        log.setHours(dto.getHours());
        log.setQuality(dto.getQuality());
        log.setNotes(dto.getNotes());
        return sleepLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<SleepLog> recent(User user) {
        return sleepLogRepository.findTop14ByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<SleepLog> between(User user, LocalDate start, LocalDate end) {
        return sleepLogRepository.findByUserAndEntryDateBetweenOrderByEntryDateDesc(user, start, end);
    }

    @Transactional(readOnly = true)
    public double weeklyAverage(User user) {
        return sleepLogRepository.averageHoursSince(user, LocalDate.now().minusDays(6));
    }
}
