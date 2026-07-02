package com.cyclecare.nutrition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FoodDatabase {

    private final Map<String, FoodInfo> foods = new HashMap<>();

    @PostConstruct
    public void loadFoods() {

        try {

            ObjectMapper mapper = new ObjectMapper();

            InputStream input =
                    new ClassPathResource("nutrition/foods.json")
                            .getInputStream();

            List<FoodInfo> list =
                    mapper.readValue(
                            input,
                            new TypeReference<List<FoodInfo>>() {
                            });

            for (FoodInfo food : list) {

                foods.put(
                        food.getName().toLowerCase(),
                        food
                );
            }

            System.out.println(
                    "Loaded "
                            + foods.size()
                            + " foods."
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to load foods.json",
                    e
            );

        }

    }

    public FoodInfo findFood(String name) {

        if (name == null)
            return null;

        return foods.get(name.trim().toLowerCase());

    }

    public Map<String, FoodInfo> getFoods() {
        return foods;
    }

}