package com.cyclecare.config;

import com.cyclecare.domain.ExerciseRecommendation;
import com.cyclecare.domain.ExerciseType;
import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.NutritionRecommendation;
import com.cyclecare.repository.ExerciseRecommendationRepository;
import com.cyclecare.repository.NutritionRecommendationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final NutritionRecommendationRepository nutritionRepository;
    private final ExerciseRecommendationRepository exerciseRepository;

    public DataInitializer(NutritionRecommendationRepository nutritionRepository,
                           ExerciseRecommendationRepository exerciseRepository) {
        this.nutritionRepository = nutritionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedNutrition();
        seedExercise();
    }

    private void seedNutrition() {
        if (nutritionRepository.count() > 0) {
            return;
        }

        nutritionRepository.save(nutrition(MenstrualPhase.MENSTRUAL,
                "Lentils, beans, spinach, dates, eggs, tofu, pumpkin seeds, citrus fruits, warm soups, herbal teas",
                "Excess caffeine, highly salty snacks, alcohol, very cold sugary drinks",
                "Prioritize iron-rich meals, pair plant iron with vitamin C, sip warm fluids, and keep water nearby.",
                "During bleeding, iron and fluid support matter most. Warm, easy-to-digest meals may help comfort while hydration supports energy."));

        nutritionRepository.save(nutrition(MenstrualPhase.FOLLICULAR,
                "Lean chicken, fish, yogurt, chickpeas, berries, bananas, leafy greens, whole grains, fermented foods",
                "Heavy fried foods, skipped meals, large sugar spikes",
                "Use the rising-energy window for balanced plates with protein, colorful produce, and steady carbohydrates.",
                "As estrogen rises, many people feel more energetic. Protein, fruits, vegetables, and complex carbs support recovery and focus."));

        nutritionRepository.save(nutrition(MenstrualPhase.OVULATION,
                "Berries, oranges, broccoli, carrots, flax seeds, oats, beans, avocado, water-rich fruits",
                "Excess processed sugar, heavy greasy meals, dehydration",
                "Add antioxidant-rich produce, increase fiber, and stay hydrated especially if cervical fluid or bloating changes.",
                "Around ovulation, antioxidant and fiber-rich foods support general wellness and digestion while hydration helps comfort."));

        nutritionRepository.save(nutrition(MenstrualPhase.LUTEAL,
                "Dark chocolate in moderation, almonds, pumpkin seeds, oats, brown rice, sweet potatoes, bananas, yogurt",
                "Excess caffeine, very salty foods, high-sugar snacks that worsen cravings",
                "Choose magnesium-rich foods and complex carbohydrates. Keep snacks planned to reduce PMS-related cravings.",
                "Progesterone-dominant days can bring PMS symptoms. Magnesium, steady carbs, and regular meals may support mood and energy."));
    }

    private void seedExercise() {
        if (exerciseRepository.count() > 0) {
            return;
        }

        exerciseRepository.saveAll(List.of(
                exercise(MenstrualPhase.MENSTRUAL, ExerciseType.WALKING, "Gentle walk", "Slow comfortable walk focused on circulation and stress relief.", "Low", 20),
                exercise(MenstrualPhase.MENSTRUAL, ExerciseType.YOGA, "Restorative yoga", "Breath-led poses, hip openers, and low-pressure mobility.", "Low", 25),
                exercise(MenstrualPhase.FOLLICULAR, ExerciseType.STRENGTH_TRAINING, "Progressive strength", "Moderate full-body session with controlled sets and recovery.", "Moderate", 35),
                exercise(MenstrualPhase.FOLLICULAR, ExerciseType.CARDIO, "Light cardio", "Bike, jog, or dance session if energy feels steady.", "Moderate", 30),
                exercise(MenstrualPhase.OVULATION, ExerciseType.CARDIO, "Energizing cardio", "Short cardio session with warm-up, hydration, and cool-down.", "Moderate", 30),
                exercise(MenstrualPhase.OVULATION, ExerciseType.STRETCHING, "Mobility flow", "Stretching for hips, back, and shoulders to reduce tension.", "Low", 20),
                exercise(MenstrualPhase.LUTEAL, ExerciseType.YOGA, "PMS-support yoga", "Grounded flow with breathing and lower-back relief poses.", "Low", 25),
                exercise(MenstrualPhase.LUTEAL, ExerciseType.WALKING, "Steady walk", "Relaxed outdoor or treadmill walk to support mood and sleep.", "Low", 30)
        ));
    }

    private NutritionRecommendation nutrition(MenstrualPhase phase,
                                             String recommended,
                                             String avoid,
                                             String tips,
                                             String explanation) {
        NutritionRecommendation recommendation = new NutritionRecommendation();
        recommendation.setPhase(phase);
        recommendation.setRecommendedFoods(recommended);
        recommendation.setFoodsToAvoid(avoid);
        recommendation.setDailyTips(tips);
        recommendation.setExplanation(explanation);
        return recommendation;
    }

    private ExerciseRecommendation exercise(MenstrualPhase phase,
                                            ExerciseType type,
                                            String title,
                                            String description,
                                            String intensity,
                                            int durationMinutes) {
        ExerciseRecommendation recommendation = new ExerciseRecommendation();
        recommendation.setPhase(phase);
        recommendation.setExerciseType(type);
        recommendation.setTitle(title);
        recommendation.setDescription(description);
        recommendation.setIntensity(intensity);
        recommendation.setDurationMinutes(durationMinutes);
        return recommendation;
    }
}
