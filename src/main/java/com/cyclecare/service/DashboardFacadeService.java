package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.User;
import com.cyclecare.dto.DashboardView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DashboardFacadeService {

    private final CycleService cycleService;
    private final SymptomService symptomService;
    private final MoodService moodService;
    private final WaterService waterService;
    private final SleepService sleepService;
    private final AnalyticsService analyticsService;
    private final RecommendationService recommendationService;
    private final FlowService flowService;
    private final FlowNutritionRecommendationService flowNutritionRecommendationService;

    public DashboardFacadeService(CycleService cycleService,
                                  SymptomService symptomService,
                                  MoodService moodService,
                                  WaterService waterService,
                                  SleepService sleepService,
                                  AnalyticsService analyticsService,
                                  RecommendationService recommendationService,
                                  FlowService flowService,
                                  FlowNutritionRecommendationService flowNutritionRecommendationService) {
        this.cycleService = cycleService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.analyticsService = analyticsService;
        this.recommendationService = recommendationService;
        this.flowService = flowService;
        this.flowNutritionRecommendationService = flowNutritionRecommendationService;
    }

    @Transactional(readOnly = true)
    public DashboardView buildDashboardForUser(User user) {
        List<Cycle> recentCycles = cycleService.recentCycles(user);
        Optional<CyclePrediction> predictionOptional = cycleService.currentPrediction(recentCycles);
        CyclePrediction prediction = predictionOptional.orElse(null);
        MenstrualPhase phase = prediction != null ? prediction.getPhase() : MenstrualPhase.FOLLICULAR;
        
        int waterToday = waterService.totalForToday(user);
        double sleepAverage = sleepService.weeklyAverage(user);
        List<FlowEntry> recentFlow = flowService.recent(user, 30);
        boolean menstrualDay = prediction != null && prediction.getPhase() == MenstrualPhase.MENSTRUAL;
        
        Optional<FlowEntry> todayFlow = flowService.today(user);

        return new DashboardView(
                prediction,
                menstrualDay,
                symptomService.latest(user),
                moodService.latest(user),
                waterToday,
                sleepAverage,
                analyticsService.insights(user, predictionOptional, waterToday, sleepAverage, recentCycles, recentFlow),
                recommendationService.nutritionForPhase(phase),
                recommendationService.exercisesForPhase(phase),
                todayFlow.orElse(null),
                todayFlow.map(flowNutritionRecommendationService::forEntry).orElse(null)
        );
    }
}
