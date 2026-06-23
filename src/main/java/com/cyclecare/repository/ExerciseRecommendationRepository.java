package com.cyclecare.repository;

import com.cyclecare.domain.ExerciseRecommendation;
import com.cyclecare.domain.MenstrualPhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRecommendationRepository extends JpaRepository<ExerciseRecommendation, Long> {
    List<ExerciseRecommendation> findByPhase(MenstrualPhase phase);
}
