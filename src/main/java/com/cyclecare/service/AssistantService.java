package com.cyclecare.service;

import com.cyclecare.domain.User;
import com.cyclecare.dto.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistantService {

    private final GeminiService geminiService;

    public AssistantService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String answer(
            String question,
            User user,
            CyclePrediction prediction,
            List<ChatMessage> chatHistory) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                            You are CycleCare AI.

                            You are an educational menstrual health assistant.

                            Never diagnose diseases.

                            Never prescribe medicines.

                            Keep answers under 180 words.

                            Use markdown.

                            Current User Information

                        """);
        prompt.append("Name: ").append(user.getName()).append("\n");
        prompt.append("Age: ").append(user.getAge()).append("\n");
        prompt.append("Height: ").append(user.getHeight()).append(" cm\n");
        prompt.append("Weight: ").append(user.getWeight()).append(" kg\n");
        prompt.append("Activity: ").append(user.getActivityLevel()).append("\n\n");
        if (prediction != null) {

            prompt.append("Current Phase: ")
                    .append(prediction.getPhase())
                    .append("\n");

            prompt.append("Cycle Day: ")
                    .append(prediction.getCurrentCycleDay())
                    .append("\n");

            prompt.append("Next Period: ")
                    .append(prediction.getNextPeriodDate())
                    .append("\n\n");
        }
        prompt.append("Conversation History:\n\n");

        for (ChatMessage message : chatHistory) {

            if ("user".equals(message.getRole())) {
                prompt.append("User: ");
            } else {
                prompt.append("Assistant: ");
            }

            prompt.append(message.getMessage()).append("\n");
        }

        prompt.append("\nCurrent Question:\n");
        prompt.append(question);

        return geminiService.askGemini(prompt.toString());    }
}