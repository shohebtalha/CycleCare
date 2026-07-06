package com.cyclecare.controller;

import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.User;
import com.cyclecare.service.AnalyticsService;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.FlowNutritionRecommendationService;
import com.cyclecare.service.FlowService;
import com.cyclecare.service.MoodService;
import com.cyclecare.service.RecommendationService;
import com.cyclecare.service.SleepService;
import com.cyclecare.service.SymptomService;
import com.cyclecare.service.UserService;
import com.cyclecare.service.WaterService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;
import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final CycleService cycleService;
    private final SymptomService symptomService;
    private final MoodService moodService;
    private final WaterService waterService;
    private final SleepService sleepService;
    private final AnalyticsService analyticsService;
    private final RecommendationService recommendationService;
    private final FlowService flowService;
    private final FlowNutritionRecommendationService flowNutritionRecommendationService;

    public DashboardController(UserService userService,
                               CycleService cycleService,
                               SymptomService symptomService,
                               MoodService moodService,
                               WaterService waterService,
                               SleepService sleepService,
                               AnalyticsService analyticsService,
                               RecommendationService recommendationService,
                               FlowService flowService,
                               FlowNutritionRecommendationService flowNutritionRecommendationService) {
        this.userService = userService;
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

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        List<Cycle> recentCycles = cycleService.recentCycles(user);
        Optional<CyclePrediction> predictionOptional = cycleService.currentPrediction(recentCycles);
        CyclePrediction prediction = predictionOptional.orElse(null);
        MenstrualPhase phase = prediction != null ? prediction.getPhase() : MenstrualPhase.FOLLICULAR;
        int waterToday = waterService.totalForToday(user);
        double sleepAverage = sleepService.weeklyAverage(user);
        List<FlowEntry> recentFlow = flowService.recent(user, 30);

        model.addAttribute("prediction", prediction);
        model.addAttribute("latestSymptoms", symptomService.latest(user));
        model.addAttribute("latestMoods", moodService.latest(user));
        model.addAttribute("waterToday", waterToday);
        model.addAttribute("sleepAverage", sleepAverage);
        model.addAttribute("insights", analyticsService.insights(user, predictionOptional, waterToday, sleepAverage, recentCycles, recentFlow));
        model.addAttribute("nutrition", recommendationService.nutritionForPhase(phase));
        model.addAttribute("exercises", recommendationService.exercisesForPhase(phase));
        Optional<FlowEntry> todayFlow = flowService.today(user);
        model.addAttribute("todayFlow", todayFlow.orElse(null));

        model.addAttribute("todayFlowRecommendation",
                todayFlow
                        .map(flowNutritionRecommendationService::forEntry)
                        .orElse(null));
        return "dashboard";
    }
}
