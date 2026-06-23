package com.cyclecare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nutrition_recommendations")
public class NutritionRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 40)
    private MenstrualPhase phase;

    @Column(nullable = false, length = 900)
    private String recommendedFoods;

    @Column(nullable = false, length = 700)
    private String foodsToAvoid;

    @Column(nullable = false, length = 900)
    private String dailyTips;

    @Column(nullable = false, length = 1200)
    private String explanation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MenstrualPhase getPhase() {
        return phase;
    }

    public void setPhase(MenstrualPhase phase) {
        this.phase = phase;
    }

    public String getRecommendedFoods() {
        return recommendedFoods;
    }

    public void setRecommendedFoods(String recommendedFoods) {
        this.recommendedFoods = recommendedFoods;
    }

    public String getFoodsToAvoid() {
        return foodsToAvoid;
    }

    public void setFoodsToAvoid(String foodsToAvoid) {
        this.foodsToAvoid = foodsToAvoid;
    }

    public String getDailyTips() {
        return dailyTips;
    }

    public void setDailyTips(String dailyTips) {
        this.dailyTips = dailyTips;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
