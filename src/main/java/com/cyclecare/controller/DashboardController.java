package com.cyclecare.controller;

import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.User;
import com.cyclecare.service.AnalyticsService;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
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

    public DashboardController(UserService userService,
                               CycleService cycleService,
                               SymptomService symptomService,
                               MoodService moodService,
                               WaterService waterService,
                               SleepService sleepService,
                               AnalyticsService analyticsService,
                               RecommendationService recommendationService) {
        this.userService = userService;
        this.cycleService = cycleService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.analyticsService = analyticsService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        CyclePrediction prediction = cycleService.currentPrediction(user).orElse(null);
        MenstrualPhase phase = prediction != null ? prediction.getPhase() : MenstrualPhase.FOLLICULAR;

        model.addAttribute("prediction", prediction);
        model.addAttribute("latestSymptoms", symptomService.latest(user));
        model.addAttribute("latestMoods", moodService.latest(user));
        model.addAttribute("waterToday", waterService.totalForToday(user));
        model.addAttribute("sleepAverage", sleepService.weeklyAverage(user));
        model.addAttribute("insights", analyticsService.insights(user));
        model.addAttribute("nutrition", recommendationService.nutritionForPhase(phase));
        model.addAttribute("exercises", recommendationService.exercisesForPhase(phase));
        return "dashboard";
    }
}
