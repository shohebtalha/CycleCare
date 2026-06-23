package com.cyclecare.repository;

import com.cyclecare.domain.JournalEntry;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findTop10ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<JournalEntry> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    List<JournalEntry> findByUserOrderByEntryDateDescCreatedAtDesc(User user);
}
