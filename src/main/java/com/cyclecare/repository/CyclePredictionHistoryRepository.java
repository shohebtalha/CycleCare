package com.cyclecare.repository;

import com.cyclecare.domain.CyclePredictionHistory;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CyclePredictionHistoryRepository extends JpaRepository<CyclePredictionHistory, Long> {
    boolean existsByUserAndActualPeriodStartDate(User user, LocalDate actualPeriodStartDate);

    List<CyclePredictionHistory> findTop10ByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
