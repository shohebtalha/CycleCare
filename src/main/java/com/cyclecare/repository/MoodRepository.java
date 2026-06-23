package com.cyclecare.repository;

import com.cyclecare.domain.Mood;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findTop8ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<Mood> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    List<Mood> findByUserOrderByEntryDateDescCreatedAtDesc(User user);
}
