package com.cyclecare.nutrition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NutritionKnowledgeBase {

    private static final Logger log = LoggerFactory.getLogger(NutritionKnowledgeBase.class);

    private final Map<String, FoodInfo> foods = new HashMap<>();

    @PostConstruct
    public void load() {

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

                foods.put(food.getName().toLowerCase(), food);

                if (food.getAliases() != null) {

                    for (String alias : food.getAliases()) {

                        foods.put(alias.toLowerCase(), food);

                    }

                }

            }

            log.info("Loaded {} nutrition lookup keys.", foods.size());

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    public FoodInfo find(String food) {

        return foods.get(food.toLowerCase().trim());

    }

}
