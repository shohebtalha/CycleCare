package com.cyclecare.repository;

import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlowRepository extends JpaRepository<FlowEntry, Long> {
    List<FlowEntry> findByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<FlowEntry> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    Optional<FlowEntry> findByUserAndEntryDate(User user, LocalDate entryDate);

    Optional<FlowEntry> findTopByUserOrderByEntryDateDesc(User user);

    boolean existsByUserAndEntryDate(User user, LocalDate entryDate);

    Optional<FlowEntry> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
