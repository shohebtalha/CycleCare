package com.cyclecare.repository;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CycleRepository extends JpaRepository<Cycle, Long> {
    Optional<Cycle> findTopByUserOrderByLastPeriodStartDateDesc(User user);

    List<Cycle> findTop12ByUserOrderByLastPeriodStartDateDesc(User user);

    List<Cycle> findByUserOrderByLastPeriodStartDateDesc(User user);
}
