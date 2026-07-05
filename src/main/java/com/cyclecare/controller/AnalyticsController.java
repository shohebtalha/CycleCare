package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.service.AnalyticsService;
import com.cyclecare.service.FlowNutritionRecommendationService;
import com.cyclecare.service.FlowService;
import com.cyclecare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final FlowService flowService;
    private final FlowNutritionRecommendationService flowNutritionRecommendationService;

    public AnalyticsController(UserService userService,
                               AnalyticsService analyticsService,
                               FlowService flowService,
                               FlowNutritionRecommendationService flowNutritionRecommendationService) {
        this.userService = userService;
        this.analyticsService = analyticsService;
        this.flowService = flowService;
        this.flowNutritionRecommendationService = flowNutritionRecommendationService;
    }

    @GetMapping("/analytics")
    public String analytics(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("cycleTrend", analyticsService.cycleLengthTrend(user));
        model.addAttribute("moodTrend", analyticsService.moodTrend(user));
        model.addAttribute("symptomFrequency", analyticsService.symptomFrequency(user));
        model.addAttribute("regularityScore", analyticsService.regularityScore(user));
        model.addAttribute("alerts", analyticsService.alerts(user));
        model.addAttribute("flowDistribution", flowService.distribution(user));
        model.addAttribute("mostCommonFlowLevel", flowService.mostCommonLevel(user).orElse(null));
        model.addAttribute("flowDietCategory", flowService.mostCommonLevel(user)
                .map(flowNutritionRecommendationService::forLevel)
                .map(recommendation -> recommendation.getDietCategory())
                .orElse("Not enough data"));
        return "analytics";
    }
}
