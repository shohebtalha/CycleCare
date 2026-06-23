package com.cyclecare.repository;

import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {
    List<Symptom> findTop8ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<Symptom> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    List<Symptom> findByUserOrderByEntryDateDescCreatedAtDesc(User user);

    long countByUserAndSeverityGreaterThanEqualAndEntryDateAfter(User user, Integer severity, LocalDate after);
}
