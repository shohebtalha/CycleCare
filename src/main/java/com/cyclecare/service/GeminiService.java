package com.cyclecare.service;

import com.cyclecare.dto.Gemini.Content;
import com.cyclecare.dto.Gemini.GeminiRequest;
import com.cyclecare.dto.Gemini.Part;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Service
public class GeminiService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public GeminiService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(12));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public String askGemini(String question) {
        if (apiKey == null || apiKey.isBlank()) {
            return "CycleCare AI is not configured yet. Please add a Gemini API key and try again.";
        }

        GeminiRequest request = new GeminiRequest(
                List.of(new Content(List.of(new Part(question))))
        );

        try {
            JsonNode response = restClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("candidates").isEmpty()) {
                return "No response received from Gemini.";
            }

            JsonNode textNode = response.path("candidates").path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");
            String answer = textNode.asText();
            return answer.isBlank() ? "No response received from Gemini." : answer;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return switch (e.getStatusCode().value()) {
                case 400 -> "The AI request was invalid.";
                case 401 -> "Gemini API key is invalid.";
                case 403 -> "Access to Gemini API is denied.";
                case 429 -> "CycleCare AI is temporarily busy. Please try again in a minute.";
                default -> "Unable to contact the AI service.";
            };
        } catch (Exception e) {
            return "Sorry, something went wrong while contacting the AI service. Please try again later.";
        }
    }
}
