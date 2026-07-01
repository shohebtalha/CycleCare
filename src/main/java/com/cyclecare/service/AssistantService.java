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

        boolean firstMessage = chatHistory.size() <= 1;
        String greetingInstruction = "";

        if (firstMessage) {

            greetingInstruction = """
        Begin your response with a warm greeting using the user's first name.
        Example: "Hi Sunena! 👋"

        This greeting should appear ONLY once at the beginning of a new conversation.

        Never greet the user again in later responses.
        """;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append( """
                            You are CycleCare AI.
                            
                            %s
                            
                            Current menstrual phase:
                            %s
                            
                            Rules:
                            
                            - Give educational information only.
                            - Never diagnose diseases.
                            - Never prescribe medicines.
                            - Keep answers under 200 words.
                            - Use simple language.
                            - Continue the conversation naturally.
                            - Do NOT repeat greetings after the first response.
                            
                            User Question:
                            
                            %s
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