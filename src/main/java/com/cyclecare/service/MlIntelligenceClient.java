package com.cyclecare.service;

import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.WaterLog;
import com.cyclecare.dto.HealthIntelligenceCard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MlIntelligenceClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final boolean enabled;

    public MlIntelligenceClient(@Value("${cyclecare.ml.base-url:http://localhost:8000}") String baseUrl,
                                @Value("${cyclecare.ml.enabled:true}") boolean enabled) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
        this.baseUrl = baseUrl;
        this.enabled = enabled;
    }

    public CompletableFuture<MlIntelligenceResult> insights(List<FlowEntry> flowEntries,
                                                            List<Symptom> symptoms,
                                                            List<Mood> moods,
                                                            List<SleepLog> sleepLogs,
                                                            List<WaterLog> waterLogs) {
        if (!enabled) {
            return CompletableFuture.completedFuture(MlIntelligenceResult.unavailable());
        }

        MlInsightRequest request = MlInsightRequest.from(flowEntries, symptoms, moods, sleepLogs, waterLogs);
        return CompletableFuture.supplyAsync(() -> {
            try {
                MlInsightResponse response = restClient.post()
                        .uri(baseUrl + "/api/ml/insights")
                        .body(request)
                        .retrieve()
                        .body(MlInsightResponse.class);
                if (response == null) {
                    return MlIntelligenceResult.unavailable();
                }
                HealthMlPrediction prediction = response.toPrediction();
                return new MlIntelligenceResult(
                        response.cards() == null ? List.of() : response.cards(),
                        prediction,
                        prediction != null
                );
            } catch (Exception ignored) {
                return MlIntelligenceResult.unavailable();
            }
        });
    }

    private record MlInsightRequest(
            List<MlFlowEntry> flowEntries,
            List<MlSymptom> symptoms,
            List<MlMood> moods,
            List<MlSleepLog> sleepLogs,
            List<MlWaterLog> waterLogs
    ) {
        static MlInsightRequest from(List<FlowEntry> flowEntries,
                                     List<Symptom> symptoms,
                                     List<Mood> moods,
                                     List<SleepLog> sleepLogs,
                                     List<WaterLog> waterLogs) {
            return new MlInsightRequest(
                    flowEntries.stream()
                            .map(entry -> new MlFlowEntry(entry.getEntryDate().toString(), entry.getFlowLevel().name()))
                            .toList(),
                    symptoms.stream()
                            .map(symptom -> new MlSymptom(
                                    symptom.getEntryDate().toString(),
                                    symptom.getType().name(),
                                    symptom.getSeverity()))
                            .toList(),
                    moods.stream()
                            .map(mood -> new MlMood(
                                    mood.getEntryDate().toString(),
                                    mood.getType().name(),
                                    mood.getIntensity()))
                            .toList(),
                    sleepLogs.stream()
                            .map(log -> new MlSleepLog(log.getEntryDate().toString(), log.getHours()))
                            .toList(),
                    waterLogs.stream()
                            .map(log -> new MlWaterLog(log.getEntryDate().toString(), log.getAmountMl()))
                            .toList()
            );
        }
    }

    private record MlFlowEntry(String date, String flowLevel) {
    }

    private record MlSymptom(String date, String type, Integer severity) {
    }

    private record MlMood(String date, String type, Integer intensity) {
    }

    private record MlSleepLog(String date, Double hours) {
    }

    private record MlWaterLog(String date, Integer amount) {
    }

    private record MlInsightResponse(
            List<HealthIntelligenceCard> cards,
            Integer adaptiveRiskScore,
            String riskLevel,
            Integer modelConfidence,
            String primaryDriver,
            String nextBestAction,
            List<String> drivers
    ) {
        HealthMlPrediction toPrediction() {
            if (adaptiveRiskScore == null || modelConfidence == null) {
                return null;
            }
            return new HealthMlPrediction(
                    adaptiveRiskScore,
                    riskLevel == null || riskLevel.isBlank() ? "Learning" : riskLevel,
                    modelConfidence,
                    primaryDriver == null || primaryDriver.isBlank()
                            ? "CycleCare found a signal but could not explain the strongest driver."
                            : primaryDriver,
                    nextBestAction == null || nextBestAction.isBlank()
                            ? "Keep logging cycle, symptom, mood, water, and sleep data together."
                            : nextBestAction,
                    drivers == null || drivers.isEmpty() ? List.of("Pattern learning") : drivers
            );
        }
    }
}
