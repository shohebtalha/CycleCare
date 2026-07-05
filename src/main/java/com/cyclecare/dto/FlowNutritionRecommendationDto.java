package com.cyclecare.dto;

import com.cyclecare.domain.FlowLevel;

import java.util.List;

public class FlowNutritionRecommendationDto {

    private final FlowLevel flowLevel;
    private final String dietCategory;
    private final String summary;
    private final List<String> focusNutrients;
    private final List<String> healthyFoods;
    private final List<String> foodsToLimit;
    private final List<String> hydrationTips;

    public FlowNutritionRecommendationDto(FlowLevel flowLevel,
                                          String dietCategory,
                                          String summary,
                                          List<String> focusNutrients,
                                          List<String> healthyFoods,
                                          List<String> foodsToLimit,
                                          List<String> hydrationTips) {
        this.flowLevel = flowLevel;
        this.dietCategory = dietCategory;
        this.summary = summary;
        this.focusNutrients = focusNutrients;
        this.healthyFoods = healthyFoods;
        this.foodsToLimit = foodsToLimit;
        this.hydrationTips = hydrationTips;
    }

    public FlowLevel getFlowLevel() {
        return flowLevel;
    }

    public String getDietCategory() {
        return dietCategory;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getFocusNutrients() {
        return focusNutrients;
    }

    public List<String> getHealthyFoods() {
        return healthyFoods;
    }

    public List<String> getFoodsToLimit() {
        return foodsToLimit;
    }

    public List<String> getHydrationTips() {
        return hydrationTips;
    }
}
