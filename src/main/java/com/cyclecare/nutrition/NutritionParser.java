package com.cyclecare.nutrition;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NutritionParser {

    public List<String> extractFoods(String nutritionLog){

        List<String> foods=new ArrayList<>();

        if(nutritionLog==null || nutritionLog.isBlank()){
            return foods;
        }

        String[] lines=nutritionLog.split("\\R");

        for (String line : lines) {

            line = line.trim();

            if (line.isBlank())
                continue;

            // Remove "Breakfast:", "Lunch:", etc.
            if (line.contains(":")) {
                line = line.substring(line.indexOf(':') + 1);
            }

            String[] tokens = line.split(",");

            for (String token : tokens) {

                token = token.trim().toLowerCase();

                if (!token.isBlank()) {
                    foods.add(token);
                }
            }
        }

        return foods;

    }

}