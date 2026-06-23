package com.cyclecare.service;

import com.cyclecare.domain.ExerciseRecommendation;
import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.NutritionRecommendation;
import com.cyclecare.repository.ExerciseRecommendationRepository;
import com.cyclecare.repository.NutritionRecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendationService {

    private final NutritionRecommendationRepository nutritionRepository;
    private final ExerciseRecommendationRepository exerciseRepository;

    public RecommendationService(NutritionRecommendationRepository nutritionRepository,
                                 ExerciseRecommendationRepository exerciseRepository) {
        this.nutritionRepository = nutritionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional(readOnly = true)
    public NutritionRecommendation nutritionForPhase(MenstrualPhase phase) {
        return nutritionRepository.findByPhase(phase)
                .orElseThrow(() -> new IllegalStateException("Nutrition recommendations are not seeded."));
    }

    @Transactional(readOnly = true)
    public List<ExerciseRecommendation> exercisesForPhase(MenstrualPhase phase) {
        return exerciseRepository.findByPhase(phase);
    }

    @Transactional(readOnly = true)
    public List<NutritionRecommendation> allNutrition() {
        return nutritionRepository.findAll();
    }
}
