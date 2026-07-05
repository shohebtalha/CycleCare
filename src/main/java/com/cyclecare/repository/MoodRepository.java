package com.cyclecare.repository;

import com.cyclecare.domain.Mood;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findTop8ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<Mood> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    List<Mood> findByUserOrderByEntryDateDescCreatedAtDesc(User user);

    Optional<Mood> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
