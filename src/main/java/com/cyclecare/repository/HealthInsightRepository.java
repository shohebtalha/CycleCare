package com.cyclecare.repository;

import com.cyclecare.domain.HealthInsight;
import com.cyclecare.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthInsightRepository extends JpaRepository<HealthInsight, Long> {
    List<HealthInsight> findTop10ByUserOrderByInsightDateDescCreatedAtDesc(User user);

    List<HealthInsight> findByUserOrderByInsightDateDescCreatedAtDesc(User user);
}
