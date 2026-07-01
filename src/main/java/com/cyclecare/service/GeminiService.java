package com.cyclecare.service;

import com.cyclecare.dto.Gemini.Content;
import com.cyclecare.dto.Gemini.GeminiRequest;
import com.cyclecare.dto.Gemini.Part;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public GeminiService() {
        this.restClient = RestClient.create();
    }

    public String askGemini(String question) {

        GeminiRequest request = new GeminiRequest(
                List.of(
                        new Content(
                                List.of(
                                        new Part(question)
                                )
                        )
                )
        );

        try {

            JsonNode response = restClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                return "No response received from Gemini.";
            }

            return response
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            switch (e.getStatusCode().value()) {

                case 400:
                    return "The AI request was invalid.";

                case 401:
                    return "Gemini API key is invalid.";

                case 403:
                    return "Access to Gemini API is denied.";

                case 429:
                    return """
                    ⚠️ CycleCare AI is temporarily busy.

                    Please try again in a minute.
                    """;

                default:
                    return "Unable to contact the AI service.";
            }

        }catch (Exception e) {

            e.printStackTrace();

            return """
            ⚠️ Sorry, something went wrong while contacting the AI service.

            Please try again later.
            """;
        }
    }
}