package com.cyclecare.repository;

import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlowRepository extends JpaRepository<FlowEntry, Long> {
    List<FlowEntry> findByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<FlowEntry> findTop30ByUserOrderByEntryDateDescCreatedAtDesc(User user);

    List<FlowEntry> findByUserAndEntryDateBetweenOrderByEntryDateDesc(User user, LocalDate start, LocalDate end);

    Optional<FlowEntry> findByUserAndEntryDate(User user, LocalDate entryDate);

    Optional<FlowEntry> findTopByUserOrderByEntryDateDesc(User user);

    @Query("""
            select str(f.flowLevel) as label, count(f) as count
            from FlowEntry f
            where f.user = :user
            group by f.flowLevel
            """)
    List<LabelCount> countByFlowLevel(User user);

    long countByUserAndClotSizeAndEntryDateAfter(User user, com.cyclecare.domain.ClotSize clotSize, LocalDate after);

    boolean existsByUserAndEntryDate(User user, LocalDate entryDate);

    Optional<FlowEntry> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
