package com.cyclecare.repository;

import com.cyclecare.domain.Mood;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findTop8ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<Mood> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    List<Mood> findByUserOrderByEntryDateDescCreatedAtDesc(User user);

    @Query("""
            select str(m.type) as label, count(m) as count
            from Mood m
            where m.user = :user
            group by m.type
            """)
    List<LabelCount> countByMoodType(User user);

    Optional<Mood> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
