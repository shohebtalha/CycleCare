package com.cyclecare.repository;

import com.cyclecare.domain.User;
import com.cyclecare.domain.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {
    List<WaterLog> findByUserAndEntryDateOrderByCreatedAtDesc(User user, LocalDate entryDate);

    List<WaterLog> findTop14ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<WaterLog> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    @Query("select coalesce(sum(w.amountMl), 0) from WaterLog w where w.user = :user and w.entryDate = :entryDate")
    Integer totalForDate(@Param("user") User user, @Param("entryDate") LocalDate entryDate);
}
