package com.cyclecare.repository;

import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.NutritionRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionRecommendationRepository extends JpaRepository<NutritionRecommendation, Long> {
    Optional<NutritionRecommendation> findByPhase(MenstrualPhase phase);
}
