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

    public ReportService(CycleService cycleService,
                         SymptomService symptomService,
                         MoodService moodService,
                         WaterService waterService,
                         SleepService sleepService,
                         JournalService journalService,
                         AnalyticsService analyticsService,
                         NutritionAnalyzerService nutritionAnalyzerService,
                         AssistantService assistantService,
                         NutritionPromptBuilder nutritionPromptBuilder) {
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
            List<JournalEntry> journalEntries = journalService.history(user);

            addJournal(document, journalEntries);


            addNutritionAnalysis(document, journalEntries);
            addSection(document, "Analytics Summary");
            addParagraph(document, "Period regularity score: " + analyticsService.regularityScore(user) + "/100.");
            addParagraph(document, "Symptom frequency: " + analyticsService.symptomFrequency(user));
            addParagraph(document, "Mood trend: " + analyticsService.moodTrend(user));

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

    private void addJournal(Document document,
                            List<JournalEntry> entries) {

        addSection(document, "Journal Entries");

        if (entries.isEmpty()) {
            addParagraph(document, "No journal entries found.");
            return;
        }

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND);

        entries.stream().limit(5).forEach(entry -> {

            // Entry title
            Paragraph date = new Paragraph(
                    "Date : " + entry.getEntryDate(),
                    titleFont);
            date.setSpacingAfter(8);
            safeAdd(document, date);

            // Nutrition heading
            Paragraph nutritionHeading =
                    new Paragraph("Nutrition Log", titleFont);
            nutritionHeading.setSpacingAfter(6);
            safeAdd(document, nutritionHeading);

            // Nutrition table
            PdfPTable nutritionTable = new PdfPTable(2);
            nutritionTable.setWidthPercentage(100);

            try {
                nutritionTable.setWidths(new float[]{2f, 5f});
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }

            header(nutritionTable, "Meal", "Food Consumed");

            String nutrition = value(entry.getNutritionLog());

            if (!nutrition.equals("Not recorded")) {

                String[] rows = nutrition.split("\\n");

                for (String row : rows) {

                    if (!row.contains(":"))
                        continue;

                    String[] parts = row.split(":", 2);

                    nutritionTable.addCell(cell(parts[0].trim()));
                    nutritionTable.addCell(cell(parts[1].trim()));
                }

            } else {

                row(nutritionTable,
                        "Nutrition",
                        "Not recorded");
            }

            safeAdd(document, nutritionTable);

            Paragraph notesHeading =
                    new Paragraph("Additional Notes", titleFont);
            notesHeading.setSpacingBefore(6);
            safeAdd(document, notesHeading);

            addParagraph(document,
                    value(entry.getObservations()));

            Paragraph divider =
                    new Paragraph("────────────────────────────────────────");
            divider.setSpacingAfter(12);
            safeAdd(document, divider);

        });
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
