package com.cyclecare.service;

import com.cyclecare.domain.User;
import com.cyclecare.dto.ChatMessage;
import com.cyclecare.nutrition.NutritionAnalysis;
import com.cyclecare.nutrition.NutritionPromptBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistantService {

    private final GeminiService geminiService;

    public AssistantService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String answer(String question,
                         User user,
                         CyclePrediction prediction,
                         List<ChatMessage> chatHistory) {
        boolean firstMessage = chatHistory.size() <= 1;
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are CycleCare AI, an educational menstrual health assistant.

                Safety rules:
                - Give educational information only.
                - Never diagnose diseases.
                - Never prescribe medicines, dosages, or treatment plans.
                - Encourage urgent medical care for severe bleeding, fainting, chest pain, pregnancy concerns, or severe/worsening pain.
                - Keep answers under 200 words.
                - Use simple, supportive language.
                - Continue the conversation naturally.
                """);
        if (firstMessage) {
            prompt.append("Begin once with a warm greeting using the user's first name. Do not greet again later.\n");
        } else {
            prompt.append("Do not start with a greeting.\n");
        }

        prompt.append("\nUser context:\n");
        prompt.append("Name: ").append(user.getName()).append("\n");
        prompt.append("Age: ").append(value(user.getAge())).append("\n");
        prompt.append("Height: ").append(value(user.getHeight())).append(" cm\n");
        prompt.append("Weight: ").append(value(user.getWeight())).append(" kg\n");
        prompt.append("Activity: ").append(value(user.getActivityLevel())).append("\n\n");

        if (prediction != null) {
            prompt.append("Current cycle context:\n");
            prompt.append("Phase: ").append(prediction.getPhase()).append("\n");
            prompt.append("Cycle day: ").append(prediction.getCurrentCycleDay()).append("\n");
            prompt.append("Next period: ").append(prediction.getNextPeriodDate()).append("\n\n");
        }

        prompt.append("Conversation history:\n");
        for (ChatMessage message : chatHistory) {
            prompt.append("user".equals(message.getRole()) ? "User: " : "Assistant: ");
            prompt.append(message.getMessage()).append("\n");
        }

        prompt.append("\nCurrent question:\n");
        prompt.append(question);

        return geminiService.askGemini(prompt.toString());
    }

    public String generateNutritionReport(NutritionAnalysis analysis,
                                          NutritionPromptBuilder promptBuilder) {
        String prompt = promptBuilder.build(analysis);
        return geminiService.askGemini(prompt);
    }

    private String value(Object value) {
        return value == null ? "Not provided" : value.toString();
    }
}
