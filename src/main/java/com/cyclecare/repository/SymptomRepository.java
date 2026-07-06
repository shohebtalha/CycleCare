package com.cyclecare.repository;

import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {
    List<Symptom> findTop8ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<Symptom> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    List<Symptom> findByUserOrderByEntryDateDescCreatedAtDesc(User user);

    long countByUserAndSeverityGreaterThanEqualAndEntryDateAfter(User user, Integer severity, LocalDate after);

    @Query("""
            select
                case
                    when s.type = com.cyclecare.domain.SymptomType.OTHER
                         and s.customSymptom is not null
                         and s.customSymptom <> ''
                    then s.customSymptom
                    else str(s.type)
                end as label,
                count(s) as count
            from Symptom s
            where s.user = :user
            group by
                case
                    when s.type = com.cyclecare.domain.SymptomType.OTHER
                         and s.customSymptom is not null
                         and s.customSymptom <> ''
                    then s.customSymptom
                    else str(s.type)
                end
            order by count(s) desc
            """)
    List<LabelCount> countBySymptomLabel(User user);

    Optional<Symptom> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
