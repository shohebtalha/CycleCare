package com.cyclecare.dto;

import com.cyclecare.domain.ExerciseRecommendation;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.NutritionRecommendation;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.HealthInsight;
import com.cyclecare.service.CyclePrediction;

import java.util.List;

/**
 * Aggregates all data required to render the dashboard into a single object.
 */
public record DashboardView(
        CyclePrediction prediction,
        boolean menstrualDay,
        List<Symptom> latestSymptoms,
        List<Mood> latestMoods,
        int waterToday,
        double sleepAverage,
        List<HealthInsight> insights,
        NutritionRecommendation nutrition,
        List<ExerciseRecommendation> exercises,
        FlowEntry todayFlow,
        FlowNutritionRecommendationDto todayFlowRecommendation
) {
    public MenstrualPhase phase() {
        return prediction != null ? prediction.getPhase() : MenstrualPhase.FOLLICULAR;
    }
}
