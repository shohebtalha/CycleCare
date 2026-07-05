package com.cyclecare.service;

import com.cyclecare.domain.*;
import com.cyclecare.nutrition.FoodInfo;
import com.cyclecare.nutrition.NutritionAnalysis;
import com.cyclecare.nutrition.NutritionAnalyzerService;
import com.cyclecare.nutrition.NutritionPromptBuilder;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final Color BRAND = new Color(121, 78, 139);

    private final CycleService cycleService;
    private final SymptomService symptomService;
    private final MoodService moodService;
    private final WaterService waterService;
    private final SleepService sleepService;
    private final JournalService journalService;
    private final AnalyticsService analyticsService;
    private final NutritionAnalyzerService nutritionAnalyzerService;
    private final NutritionPromptBuilder nutritionPromptBuilder;
    private final AssistantService assistantService;
    private final FlowService flowService;
    private final FlowNutritionRecommendationService flowNutritionRecommendationService;

    public ReportService(CycleService cycleService,
                         SymptomService symptomService,
                         MoodService moodService,
                         WaterService waterService,
                         SleepService sleepService,
                         JournalService journalService,
                         AnalyticsService analyticsService,
                         NutritionAnalyzerService nutritionAnalyzerService,
                         AssistantService assistantService,
                         NutritionPromptBuilder nutritionPromptBuilder,
                         FlowService flowService,
                         FlowNutritionRecommendationService flowNutritionRecommendationService) {
        this.cycleService = cycleService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.journalService = journalService;
        this.analyticsService = analyticsService;
        this.nutritionAnalyzerService = nutritionAnalyzerService;
        this.nutritionPromptBuilder = nutritionPromptBuilder;
        this.assistantService = assistantService;
        this.flowService = flowService;
        this.flowNutritionRecommendationService = flowNutritionRecommendationService;

    }

    @Transactional(readOnly = true)
    public byte[] generate(User user) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document, "CycleCare Health Report");
            addParagraph(document, "Generated for " + user.getName() + " on " + LocalDate.now() + ".");
            addParagraph(document, "Disclaimer: CycleCare is an educational tracking tool and is not a medical diagnostic tool.");

            addSection(document, "Profile");
            addParagraph(document, "Age: " + value(user.getAge()) + " | Height: " + value(user.getHeight()) + " cm | Weight: "
                    + value(user.getWeight()) + " kg | Activity: " + user.getActivityLevel().getLabel());

            addCycles(document, cycleService.allCycles(user));
            addSymptoms(document, symptomService.history(user));
            addMoods(document, moodService.history(user));
            addWater(document, waterService.between(user, LocalDate.now().minusDays(30), LocalDate.now()));
            addSleep(document, sleepService.between(user, LocalDate.now().minusDays(30), LocalDate.now()));
            List<FlowEntry> flowEntries = flowService.between(user, LocalDate.now().minusDays(30), LocalDate.now());
            addFlow(document, flowEntries);
            addFlowNutrition(document, flowEntries);
            List<JournalEntry> journalEntries = journalService.history(user);

            addJournal(document, journalEntries);


            addNutritionAnalysis(document, journalEntries);
            addSection(document, "Analytics Summary");
            addParagraph(document, "Period regularity score: " + analyticsService.regularityScore(user) + "/100.");
            addParagraph(document, "Symptom frequency: " + analyticsService.symptomFrequency(user));
            addParagraph(document, "Mood trend: " + analyticsService.moodTrend(user));
            addParagraph(document, "Blood flow distribution: " + flowService.distribution(user));

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to generate PDF report.", ex);
        }
    }

    private void addTitle(Document document, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(16);
        safeAdd(document, paragraph);
    }

    private void addSection(Document document, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(8);
        safeAdd(document, paragraph);
    }

    private void addParagraph(Document document, String text) {
        Paragraph paragraph = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 10));
        paragraph.setSpacingAfter(8);
        safeAdd(document, paragraph);
    }

    private void addCycles(Document document, List<Cycle> cycles) {
        addSection(document, "Cycle History");
        PdfPTable table = table(4);
        header(table, "Start date", "Cycle length", "Period duration", "Notes");
        cycles.stream().limit(12).forEach(cycle -> row(table,
                cycle.getLastPeriodStartDate().toString(),
                cycle.getAverageCycleLength() + " days",
                cycle.getAveragePeriodDuration() + " days",
                value(cycle.getNotes())));
        safeAdd(document, table);
    }

    private void addSymptoms(Document document, List<Symptom> symptoms) {
        addSection(document, "Symptom History");
        PdfPTable table = table(4);
        header(table, "Date", "Symptom", "Severity", "Notes");
        symptoms.stream().limit(20).forEach(symptom -> row(table,
                symptom.getEntryDate().toString(),
                symptom.getType().getLabel(),
                String.valueOf(symptom.getSeverity()),
                value(symptom.getNotes())));
        safeAdd(document, table);
    }

    private void addMoods(Document document, List<Mood> moods) {
        addSection(document, "Mood History");
        PdfPTable table = table(4);
        header(table, "Date", "Mood", "Intensity", "Notes");
        moods.stream().limit(20).forEach(mood -> row(table,
                mood.getEntryDate().toString(),
                mood.getType().getLabel(),
                String.valueOf(mood.getIntensity()),
                value(mood.getNotes())));
        safeAdd(document, table);
    }

    private void addWater(Document document, List<WaterLog> logs) {
        addSection(document, "Water Intake");
        PdfPTable table = table(2);
        header(table, "Date", "Amount");
        logs.stream().limit(20).forEach(log -> row(table, log.getEntryDate().toString(), log.getAmountMl() + " ml"));
        safeAdd(document, table);
    }

    private void addSleep(Document document, List<SleepLog> logs) {
        addSection(document, "Sleep");
        PdfPTable table = table(4);
        header(table, "Date", "Hours", "Quality", "Notes");
        logs.stream().limit(20).forEach(log -> row(table,
                log.getEntryDate().toString(),
                String.valueOf(log.getHours()),
                log.getQuality().getLabel(),
                value(log.getNotes())));
        safeAdd(document, table);
    }

    private void addFlow(Document document, List<FlowEntry> entries) {
        addSection(document, "Blood Flow History");
        if (entries.isEmpty()) {
            addParagraph(document, "No blood flow entries found.");
            return;
        }
        PdfPTable table = table(5);
        header(table, "Date", "Flow", "Color", "Clots", "Notes");
        entries.stream().limit(20).forEach(entry -> row(table,
                entry.getEntryDate().toString(),
                entry.getFlowLevel().getLabel(),
                entry.getFlowColor().getLabel(),
                entry.getClotSize().getLabel(),
                value(entry.getNotes())));
        safeAdd(document, table);
        addParagraph(document, flowService.summary(entries));
    }

    private void addFlowNutrition(Document document, List<FlowEntry> entries) {
        addSection(document, "Flow-Based Nutrition Recommendations");
        if (entries.isEmpty()) {
            addParagraph(document, "No blood flow entries available for nutrition recommendations.");
            return;
        }
        flowService.mostCommonLevel(entries).ifPresentOrElse(level -> {
            long heavyDays = entries.stream()
                    .filter(entry -> entry.getFlowLevel() == FlowLevel.HEAVY || entry.getFlowLevel() == FlowLevel.VERY_HEAVY)
                    .count();
            var recommendation = flowNutritionRecommendationService.forLevel(level);
            addParagraph(document, "This month your blood flow was predominantly " + level.getLabel()
                    + " with " + heavyDays + " heavy day(s).");
            addParagraph(document, "Recommended focus: " + String.join(", ", recommendation.getFocusNutrients()) + ".");
            addParagraph(document, "Healthy foods: " + String.join(", ", recommendation.getHealthyFoods()) + ".");
            if (!recommendation.getFoodsToLimit().isEmpty()) {
                addParagraph(document, "Foods to limit: " + String.join(", ", recommendation.getFoodsToLimit()) + ".");
            }
            addParagraph(document, "Hydration: " + String.join(" ", recommendation.getHydrationTips()));
        }, () -> addParagraph(document, "No dominant flow level found."));
    }

    private void addJournal(Document document,
                            List<JournalEntry> entries) {

        addSection(document, "Journal Entries");

        if (entries.isEmpty()) {
            addParagraph(document, "No journal entries found.");
            return;
        }

        PdfPTable journalTable = table(6);

        try {
            journalTable.setWidths(new float[]{1.5f, 2f, 2f, 2f, 2f, 3f});
        } catch (DocumentException e) {
            throw new IllegalStateException("Unable to size journal entries table.", e);
        }

        header(journalTable, "Date", "Breakfast", "Lunch", "Snacks", "Dinner", "Additional Notes");

        entries.stream().limit(20).forEach(entry -> {
            Map<String, String> meals = nutritionMeals(entry.getNutritionLog());
            row(journalTable,
                    entry.getEntryDate().toString(),
                    value(meals.get("breakfast")),
                    value(meals.get("lunch")),
                    value(meals.get("snacks")),
                    value(meals.get("dinner")),
                    value(entry.getObservations()));

        });

        safeAdd(document, journalTable);
    }

    private Map<String, String> nutritionMeals(String nutritionLog) {
        Map<String, String> meals = new LinkedHashMap<>();

        if (nutritionLog == null || nutritionLog.isBlank()) {
            return meals;
        }

        String[] rows = nutritionLog.split("\\R");

        for (String row : rows) {
            if (!row.contains(":")) {
                continue;
            }

            String[] parts = row.split(":", 2);
            String meal = parts[0].trim().toLowerCase();
            String food = parts[1].trim();

            if (meal.equals("breakfast") || meal.equals("lunch") || meal.equals("snacks") || meal.equals("dinner")) {
                meals.put(meal, food);
            }
        }

        return meals;
    }

    private void addNutritionAnalysis(Document document,
                                      List<JournalEntry> entries) {

        addSection(document, "AI Nutrition Analysis");

        NutritionAnalysis combined = new NutritionAnalysis();

        Map<String, FoodInfo> healthyFoods = new LinkedHashMap<>();
        Map<String, FoodInfo> limitFoods = new LinkedHashMap<>();

        for (JournalEntry entry : entries) {

            if (entry.getNutritionLog() == null ||
                    entry.getNutritionLog().isBlank()) {
                continue;
            }

            NutritionAnalysis analysis =
                    nutritionAnalyzerService.analyze(entry.getNutritionLog());

            for (FoodInfo food : analysis.getHealthyFoods()) {
                healthyFoods.putIfAbsent(food.getName().toLowerCase(), food);
            }

            for (FoodInfo food : analysis.getFoodsToLimit()) {
                limitFoods.putIfAbsent(food.getName().toLowerCase(), food);
            }

            combined.getUnknownFoods().addAll(analysis.getUnknownFoods());
        }

        combined.getHealthyFoods().addAll(healthyFoods.values());
        combined.getFoodsToLimit().addAll(limitFoods.values());

        if (combined.getHealthyFoods().isEmpty()
                && combined.getFoodsToLimit().isEmpty()) {

            addParagraph(document,
                    "No nutrition records available.");

            return;
        }

        addSection(document, "Healthy Choices");

        PdfPTable healthyTable = table(3);

        header(
                healthyTable,
                "Food",
                "Health Benefit",
                "Cycle Benefit"
        );

        for (FoodInfo food : combined.getHealthyFoods()) {

            row(
                    healthyTable,
                    food.getName(),
                    value(food.getBenefit()),
                    value(food.getPeriodBenefit())
            );
        }

        safeAdd(document, healthyTable);

        addSection(document, "Foods To Limit");

        if (combined.getFoodsToLimit().isEmpty()) {

            addParagraph(
                    document,
                    "✔ Excellent! No unhealthy foods detected."
            );

        } else {

            PdfPTable limitTable = table(2);

            header(limitTable,
                    "Food",
                    "Reason");

            for (FoodInfo food : combined.getFoodsToLimit()) {

                row(
                        limitTable,
                        food.getName(),
                        value(food.getWarning())
                );
            }

            safeAdd(document, limitTable);
        }
        String aiSummary =
                assistantService.generateNutritionReport(
                        combined,
                        nutritionPromptBuilder
                );

        addSection(document, "AI Wellness Insight");

        addParagraph(document, aiSummary);

    }

    private PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        return table;
    }

    private void header(PdfPTable table, String... values) {
        for (String value : values) {
            PdfPCell cell = cell(value);
            cell.setBackgroundColor(new Color(241, 234, 246));
            table.addCell(cell);
        }
    }

    private void row(PdfPTable table, String... values) {
        for (String value : values) {
            table.addCell(cell(value));
        }
    }

    private PdfPCell cell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(5);
        cell.setBorderColor(new Color(220, 211, 226));
        return cell;
    }

    private void safeAdd(Document document, Element element) {
        try {
            document.add(element);
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to write PDF content.", ex);
        }
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "Not recorded" : value.toString();
    }
}
