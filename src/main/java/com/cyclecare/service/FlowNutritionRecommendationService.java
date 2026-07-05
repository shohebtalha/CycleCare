package com.cyclecare.service;

import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.FlowLevel;
import com.cyclecare.dto.FlowNutritionRecommendationDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowNutritionRecommendationService {

    private static final List<String> HEAVY_LIMITS = List.of(
            "Sugary drinks", "Alcohol", "Excess caffeine", "Highly processed foods",
            "Deep fried foods", "High sodium snacks"
    );

    public FlowNutritionRecommendationDto forEntry(FlowEntry entry) {
        return entry == null ? null : forLevel(entry.getFlowLevel());
    }

    public FlowNutritionRecommendationDto forLevel(FlowLevel level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case VERY_HEAVY -> new FlowNutritionRecommendationDto(
                    level,
                    "Iron, Protein and Hydration Support",
                    "Increase iron-rich foods today and pair them with vitamin C. Add protein and steady hydration.",
                    List.of("Iron", "Folate", "Vitamin C", "Protein", "Calcium"),
                    List.of("Spinach", "Amaranth", "Drumstick leaves", "Beetroot", "Pomegranate", "Dates",
                            "Raisins", "Black chana", "Rajma", "Lentils", "Soybeans", "Eggs", "Chicken",
                            "Fish", "Paneer", "Tofu", "Broccoli", "Citrus fruits", "Kiwi"),
                    HEAVY_LIMITS,
                    List.of("Drink at least 2.5-3 liters of water today.", "Add water steadily through the day.")
            );
            case HEAVY -> new FlowNutritionRecommendationDto(
                    level,
                    "Balanced Iron-Rich Diet",
                    "Choose iron-rich meals, high-protein foods, complex carbohydrates and electrolyte-rich drinks.",
                    List.of("Iron", "Protein", "Complex carbohydrates", "Electrolytes"),
                    List.of("Palak dal", "Moong dal", "Chickpeas", "Brown rice", "Millets", "Banana",
                            "Curd", "Buttermilk", "Pumpkin seeds"),
                    HEAVY_LIMITS,
                    List.of("Increase water intake today.", "Use buttermilk or other electrolyte-rich drinks if helpful.")
            );
            case MODERATE -> new FlowNutritionRecommendationDto(
                    level,
                    "Balanced Iron-Rich Diet",
                    "Maintain a balanced diet with vegetables, whole grains, fruits, lean proteins, nuts and seeds.",
                    List.of("Iron", "Protein", "Fiber", "Micronutrients"),
                    List.of("Vegetables", "Whole grains", "Seasonal fruits", "Lean proteins", "Nuts", "Seeds"),
                    List.of(),
                    List.of("Maintain hydration and balanced meals.")
            );
            case LIGHT -> new FlowNutritionRecommendationDto(
                    level,
                    "Energy and Recovery Support",
                    "Support energy and recovery with fruit, protein, vegetables, dry fruits and healthy fats.",
                    List.of("Protein", "Healthy fats", "Vitamins"),
                    List.of("Fresh fruits", "Greek yogurt", "Paneer", "Mixed vegetables", "Dry fruits", "Healthy fats"),
                    List.of(),
                    List.of("Keep sipping water through the day.")
            );
            case SPOTTING -> new FlowNutritionRecommendationDto(
                    level,
                    "Light Nutritious Meals",
                    "Choose light nutritious meals that are easy to digest.",
                    List.of("Protein", "Fluids", "Micronutrients"),
                    List.of("Soup", "Fruit", "Salad", "Moong dal", "Curd rice", "Idli", "Steamed vegetables"),
                    List.of(),
                    List.of("Keep hydration gentle and regular.")
            );
        };
    }
}
