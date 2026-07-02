package com.cyclecare.nutrition;

import java.util.ArrayList;
import java.util.List;

public class NutritionAnalysis {

    private List<FoodInfo> healthyFoods = new ArrayList<>();

    private List<FoodInfo> foodsToLimit = new ArrayList<>();

    private List<String> unknownFoods = new ArrayList<>();

    private List<String> recommendations = new ArrayList<>();

    private int nutritionScore = 100;

    public List<FoodInfo> getHealthyFoods() {
        return healthyFoods;
    }

    public List<FoodInfo> getFoodsToLimit() {
        return foodsToLimit;
    }

    public List<String> getUnknownFoods() {
        return unknownFoods;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public int getNutritionScore() {
        return nutritionScore;
    }

    public void setNutritionScore(int nutritionScore) {
        this.nutritionScore = nutritionScore;
    }
    public int getHealthyCount() {
        return healthyFoods.size();
    }

    public int getLimitCount() {
        return foodsToLimit.size();
    }
}