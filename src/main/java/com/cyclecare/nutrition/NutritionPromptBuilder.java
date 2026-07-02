package com.cyclecare.nutrition;

import org.springframework.stereotype.Component;

@Component
public class NutritionPromptBuilder {

    public String build(NutritionAnalysis analysis){

        StringBuilder prompt=new StringBuilder();

        prompt.append("""
You are a women's health nutrition expert.

Write a short personalized nutrition report.

Rules:
- Maximum 180 words.
- Mention healthy foods positively.
- Mention unhealthy foods politely.
- Give practical suggestions.
- Mention menstrual wellness where relevant.
- Don't use markdown.
- Don't diagnose diseases.

Healthy foods:
""");

        for(FoodInfo food:analysis.getHealthyFoods()){

            prompt.append("- ")
                    .append(food.getName())
                    .append("\n");

        }

        prompt.append("\nFoods to limit:\n");

        for(FoodInfo food:analysis.getFoodsToLimit()){

            prompt.append("- ")
                    .append(food.getName())
                    .append("\n");

        }

        prompt.append("\nNutrition Score : ")
                .append(analysis.getNutritionScore())
                .append("/100");

        return prompt.toString();

    }

}