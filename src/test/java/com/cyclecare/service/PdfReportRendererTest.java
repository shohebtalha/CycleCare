package com.cyclecare.service;

import com.cyclecare.domain.ActivityLevel;
import com.cyclecare.domain.ClotSize;
import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.FlowColor;
import com.cyclecare.domain.FlowEntry;
import com.cyclecare.domain.FlowLevel;
import com.cyclecare.domain.JournalEntry;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.MoodType;
import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.SleepQuality;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.SymptomType;
import com.cyclecare.domain.User;
import com.cyclecare.domain.WaterLog;
import com.cyclecare.dto.FlowNutritionRecommendationDto;
import com.cyclecare.nutrition.FoodInfo;
import com.cyclecare.nutrition.NutritionAnalysis;
import com.cyclecare.nutrition.NutritionAnalyzerService;
import com.cyclecare.nutrition.NutritionPromptBuilder;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfReportRendererTest {

    @Test
    void rendersPolishedPreviewPdf() throws Exception {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        NutritionAnalyzerService nutritionAnalyzerService = mock(NutritionAnalyzerService.class);
        AssistantService assistantService = mock(AssistantService.class);
        NutritionPromptBuilder nutritionPromptBuilder = mock(NutritionPromptBuilder.class);
        FlowService flowService = mock(FlowService.class);
        FlowNutritionRecommendationService flowNutritionRecommendationService = mock(FlowNutritionRecommendationService.class);

        when(analyticsService.regularityScore(any())).thenReturn(86);
        when(flowService.summary(any())).thenReturn("Flow was mostly moderate, with one heavier day logged.");
        when(flowService.mostCommonLevel(org.mockito.ArgumentMatchers.<List<FlowEntry>>any()))
                .thenReturn(java.util.Optional.of(FlowLevel.MODERATE));
        when(flowNutritionRecommendationService.forLevel(any())).thenReturn(new FlowNutritionRecommendationDto(
                FlowLevel.MODERATE,
                "Balanced support",
                "Moderate flow support",
                List.of("Iron", "Magnesium", "Hydration"),
                List.of("Spinach", "Paneer", "Lentils", "Citrus fruits"),
                List.of("Excess caffeine"),
                List.of("Aim for steady water intake across the day.")
        ));
        NutritionAnalysis analysis = new NutritionAnalysis();
        analysis.getHealthyFoods().add(food("Spinach", "Supports iron intake", "Useful around bleeding days", null));
        analysis.getHealthyFoods().add(food("Greek yogurt", "Protein and calcium", "Supports energy stability", null));
        when(nutritionAnalyzerService.analyze(any())).thenReturn(analysis);
        when(assistantService.generateNutritionReport(any(), any()))
                .thenReturn("Meals look supportive overall. Keep pairing iron-rich foods with vitamin C and maintain steady hydration.");

        PdfReportRenderer renderer = new PdfReportRenderer(
                analyticsService,
                nutritionAnalyzerService,
                assistantService,
                nutritionPromptBuilder,
                flowService,
                flowNutritionRecommendationService
        );

        LocalDate end = LocalDate.of(2026, 7, 8);
        ReportData reportData = new ReportData(
                user(),
                ReportRange.PAST_MONTH,
                end.minusDays(29),
                end,
                List.of(cycle(end.minusDays(14), 29), cycle(end.minusDays(43), 30)),
                List.of(symptom(end.minusDays(5), SymptomType.CRAMPS, 3), symptom(end.minusDays(4), SymptomType.FATIGUE, 2)),
                List.of(mood(end.minusDays(3), MoodType.CALM, 4), mood(end.minusDays(2), MoodType.HAPPY, 5)),
                List.of(water(end.minusDays(2), 2200), water(end.minusDays(1), 1900)),
                List.of(sleep(end.minusDays(2), 7.5, SleepQuality.GOOD), sleep(end.minusDays(1), 6.8, SleepQuality.FAIR)),
                List.of(flow(end.minusDays(5), FlowLevel.MODERATE), flow(end.minusDays(4), FlowLevel.HEAVY)),
                List.of(journal(end.minusDays(2)))
        );

        byte[] pdf = renderer.render(reportData);

        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(pdf.length).isGreaterThan(2500);
        Path preview = Path.of("target", "cyclecare-report-preview.pdf");
        Files.createDirectories(preview.getParent());
        Files.write(preview, pdf);
    }

    private User user() {
        User user = new User();
        user.setName("Report Tester");
        user.setEmail("report-preview@example.com");
        user.setPasswordHash("hash");
        user.setActivityLevel(ActivityLevel.MODERATE);
        user.setAge(24);
        user.setHeight(164.0);
        user.setWeight(58.0);
        return user;
    }

    private Cycle cycle(LocalDate date, int actualLength) {
        Cycle cycle = new Cycle();
        cycle.setLastPeriodStartDate(date);
        cycle.setAverageCycleLength(29);
        cycle.setAveragePeriodDuration(5);
        cycle.setActualCycleLength(actualLength);
        return cycle;
    }

    private Symptom symptom(LocalDate date, SymptomType type, int severity) {
        Symptom symptom = new Symptom();
        symptom.setEntryDate(date);
        symptom.setType(type);
        symptom.setSeverity(severity);
        return symptom;
    }

    private Mood mood(LocalDate date, MoodType type, int intensity) {
        Mood mood = new Mood();
        mood.setEntryDate(date);
        mood.setType(type);
        mood.setIntensity(intensity);
        return mood;
    }

    private WaterLog water(LocalDate date, int amountMl) {
        WaterLog waterLog = new WaterLog();
        waterLog.setEntryDate(date);
        waterLog.setAmountMl(amountMl);
        return waterLog;
    }

    private SleepLog sleep(LocalDate date, double hours, SleepQuality quality) {
        SleepLog sleepLog = new SleepLog();
        sleepLog.setEntryDate(date);
        sleepLog.setHours(hours);
        sleepLog.setQuality(quality);
        return sleepLog;
    }

    private FlowEntry flow(LocalDate date, FlowLevel level) {
        FlowEntry flowEntry = new FlowEntry();
        flowEntry.setEntryDate(date);
        flowEntry.setFlowLevel(level);
        flowEntry.setFlowColor(FlowColor.BRIGHT_RED);
        flowEntry.setClotSize(ClotSize.SMALL);
        return flowEntry;
    }

    private JournalEntry journal(LocalDate date) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setEntryDate(date);
        journalEntry.setNutritionLog("Breakfast: Greek yogurt\nLunch: Spinach dal\nDinner: Paneer and vegetables");
        journalEntry.setObservations("Energy was stable after lunch.");
        return journalEntry;
    }

    private FoodInfo food(String name, String benefit, String periodBenefit, String warning) {
        FoodInfo foodInfo = new FoodInfo();
        foodInfo.setName(name);
        foodInfo.setBenefit(benefit);
        foodInfo.setPeriodBenefit(periodBenefit);
        foodInfo.setWarning(warning);
        foodInfo.setHealthy(warning == null);
        return foodInfo;
    }
}
