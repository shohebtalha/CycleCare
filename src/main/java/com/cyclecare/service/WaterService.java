package com.cyclecare.service;

import com.cyclecare.domain.User;
import com.cyclecare.domain.WaterLog;
import com.cyclecare.dto.WaterLogDto;
import com.cyclecare.repository.WaterLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WaterService {

    private final WaterLogRepository waterLogRepository;

    public WaterService(WaterLogRepository waterLogRepository) {
        this.waterLogRepository = waterLogRepository;
    }

    @Transactional
    public WaterLog save(User user, WaterLogDto dto) {
        WaterLog log = new WaterLog();
        log.setUser(user);
        log.setEntryDate(dto.getEntryDate());
        log.setAmountMl(dto.getAmountMl());
        return waterLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public int totalForToday(User user) {
        return waterLogRepository.totalForDate(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<WaterLog> today(User user) {
        return waterLogRepository.findByUserAndEntryDateOrderByCreatedAtDesc(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<WaterLog> recent(User user) {
        return waterLogRepository.findTop14ByUserOrderByEntryDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<WaterLog> between(User user, LocalDate start, LocalDate end) {
        return waterLogRepository.findByUserAndEntryDateBetweenOrderByEntryDateDesc(user, start, end);
    }
}
