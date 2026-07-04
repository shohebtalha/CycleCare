package com.cyclecare.nutrition;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class NutritionAnalyzerService {

    private final NutritionParser parser;
    private final NutritionKnowledgeBase database;

    public NutritionAnalyzerService(
            NutritionParser parser,
            NutritionKnowledgeBase database) {

        this.parser = parser;
        this.database = database;
    }

    public NutritionAnalysis analyze(String nutritionLog){

        NutritionAnalysis result =
                new NutritionAnalysis();

        Set<String> foods =
                new LinkedHashSet<>(parser.extractFoods(nutritionLog));

        for(String item : foods){

            FoodInfo food =
                    database.find(item);

            if(food==null){

                result.getUnknownFoods().add(item);
                continue;

            }

            if(food.isHealthy()){

                result.getHealthyFoods().add(food);

            }else{

                result.getFoodsToLimit().add(food);

            }

        }

        int score = 100;

        score -= result.getFoodsToLimit().size()*8;

        score += result.getHealthyFoods().size()*2;

        score = Math.max(0,Math.min(100,score));

        result.setNutritionScore(score);

        if(score>=90){

            result.getRecommendations().add(
                    "Excellent dietary choices."
            );

        }else if(score>=75){

            result.getRecommendations().add(
                    "Overall diet is good. Reduce processed foods."
            );

        }else{

            result.getRecommendations().add(
                    "Increase fruits, vegetables and whole foods."
            );

        }

        return result;

    }

}