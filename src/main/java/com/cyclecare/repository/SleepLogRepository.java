package com.cyclecare.repository;

import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SleepLogRepository extends JpaRepository<SleepLog, Long> {
    List<SleepLog> findTop14ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<SleepLog> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    @Query("select coalesce(avg(s.hours), 0) from SleepLog s where s.user = :user and s.entryDate >= :after")
    Double averageHoursSince(@Param("user") User user, @Param("after") LocalDate after);
}
