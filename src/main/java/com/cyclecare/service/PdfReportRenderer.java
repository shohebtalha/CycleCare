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

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class PdfReportRenderer {

    private static final Color INK = new Color(54, 32, 45);
    private static final Color MUTED = new Color(111, 91, 103);
    private static final Color PLUM = new Color(86, 48, 91);
    private static final Color ROSE = new Color(224, 74, 112);
    private static final Color CORAL = new Color(246, 125, 101);
    private static final Color TEAL = new Color(49, 151, 149);
    private static final Color LAVENDER = new Color(245, 239, 249);
    private static final Color BLUSH = new Color(255, 241, 245);
    private static final Color MINT = new Color(235, 250, 248);
    private static final Color BORDER = new Color(226, 213, 226);
    private static final Color TABLE_STRIPE = new Color(252, 248, 251);

    private final AnalyticsService analyticsService;
    private final NutritionAnalyzerService nutritionAnalyzerService;
    private final NutritionPromptBuilder nutritionPromptBuilder;
    private final AssistantService assistantService;
    private final FlowService flowService;
    private final FlowNutritionRecommendationService flowNutritionRecommendationService;

    public PdfReportRenderer(AnalyticsService analyticsService,
                             NutritionAnalyzerService nutritionAnalyzerService,
                             AssistantService assistantService,
                             NutritionPromptBuilder nutritionPromptBuilder,
                             FlowService flowService,
                             FlowNutritionRecommendationService flowNutritionRecommendationService) {
        this.analyticsService = analyticsService;
        this.nutritionAnalyzerService = nutritionAnalyzerService;
        this.nutritionPromptBuilder = nutritionPromptBuilder;
        this.assistantService = assistantService;
        this.flowService = flowService;
        this.flowNutritionRecommendationService = flowNutritionRecommendationService;

    }

    public byte[] render(ReportData reportData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addReportHero(document, reportData.user(), reportData.range(), reportData.startDate(), reportData.endDate());
            addSnapshot(document, reportData);
            addHighlights(document, reportData.symptoms(), reportData.moods(), reportData.waterLogs(), reportData.sleepLogs(), reportData.flowEntries(), reportData.journalEntries());
            addCycleSummary(document, reportData.cycles());
            addFlowSummary(document, reportData.flowEntries());
            addWellnessSummary(document, reportData.waterLogs(), reportData.sleepLogs(), reportData.moods(), reportData.symptoms());
            addNutritionSummary(document, reportData.journalEntries());
            addCompactDetails(document, reportData.cycles(), reportData.symptoms(), reportData.moods(), reportData.waterLogs(), reportData.sleepLogs(), reportData.flowEntries(), reportData.journalEntries());
            addFooterNote(document);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to generate PDF report.", ex);
        }
    }

    private void addReportHero(Document document, User user, ReportRange range, LocalDate start, LocalDate end) {
        PdfPTable hero = new PdfPTable(2);
        hero.setWidthPercentage(100);
        try {
            hero.setWidths(new float[]{1.45f, 0.55f});
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to size report hero.", ex);
        }
        hero.setSpacingAfter(14);
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(PLUM);
        cell.setBackgroundColor(PLUM);
        cell.setPadding(20);

        Paragraph title = new Paragraph("CycleCare Health Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.WHITE));
        title.setSpacingAfter(8);
        cell.addElement(title);
        cell.addElement(new Paragraph(range.getLabel() + " - " + start + " to " + end,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(255, 207, 219))));
        cell.addElement(new Paragraph("Generated for " + user.getName() + ". This report summarizes tracked cycle, mood, wellness, flow, and nutrition patterns.",
                FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(255, 246, 249))));
        hero.addCell(cell);

        PdfPCell badge = new PdfPCell();
        badge.setBorderColor(PLUM);
        badge.setBackgroundColor(new Color(255, 236, 241));
        badge.setPadding(18);
        Paragraph badgeLabel = new Paragraph("REPORT WINDOW", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, ROSE));
        badgeLabel.setSpacingAfter(8);
        Paragraph badgeValue = new Paragraph(range.getLabel(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PLUM));
        badgeValue.setSpacingAfter(8);
        Paragraph badgeDate = new Paragraph("Generated " + LocalDate.now(), FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED));
        badge.addElement(badgeLabel);
        badge.addElement(badgeValue);
        badge.addElement(badgeDate);
        hero.addCell(badge);
        safeAdd(document, hero);

        addProfileStrip(document, user);
    }

    private void addProfileStrip(Document document, User user) {
        PdfPTable strip = table(4);
        strip.setSpacingAfter(12);
        metricCell(strip, "Age", value(user.getAge()));
        metricCell(strip, "Height", value(user.getHeight()) + " cm");
        metricCell(strip, "Weight", value(user.getWeight()) + " kg");
        metricCell(strip, "Activity", user.getActivityLevel().getLabel());
        safeAdd(document, strip);
    }

    private void addSnapshot(Document document, ReportData reportData) {
        addSection(document, "At-a-glance summary");
        PdfPTable cards = table(4);
        metricCell(cards, "Period starts", String.valueOf(reportData.cycles().size()));
        metricCell(cards, "Symptoms", String.valueOf(reportData.symptoms().size()));
        metricCell(cards, "Mood logs", String.valueOf(reportData.moods().size()));
        metricCell(cards, "Flow logs", String.valueOf(reportData.flowEntries().size()));
        metricCell(cards, "Avg water", averageWater(reportData.waterLogs()));
        metricCell(cards, "Avg sleep", averageSleep(reportData.sleepLogs()));
        metricCell(cards, "Journal days", String.valueOf(reportData.journalEntries().size()));
        metricCell(cards, "Regularity", analyticsService.regularityScore(reportData.user()) + "/100");
        safeAdd(document, cards);
    }

    private void addHighlights(Document document,
                               List<Symptom> symptoms,
                               List<Mood> moods,
                               List<WaterLog> waterLogs,
                               List<SleepLog> sleepLogs,
                               List<FlowEntry> flowEntries,
                               List<JournalEntry> journalEntries) {
        addSection(document, "Key insights");
        PdfPTable insights = table(2);
        insightCell(insights, "Cycle & flow", flowEntries.isEmpty()
                ? "No flow entries were logged in this selected range."
                : flowService.summary(flowEntries));
        insightCell(insights, "Symptoms", symptoms.isEmpty()
                ? "No symptoms were logged in this selected range."
                : mostCommonSymptom(symptoms) + " appeared most often across " + symptoms.size() + " symptom log(s).");
        insightCell(insights, "Mood", moods.isEmpty()
                ? "No mood entries were logged in this selected range."
                : "Average mood intensity was " + rounded(moods.stream().mapToInt(Mood::getIntensity).average().orElse(0)) + "/5.");
        insightCell(insights, "Wellness", "Average sleep: " + averageSleep(sleepLogs) + ". Average hydration: " + averageWater(waterLogs) + ".");
        safeAdd(document, insights);

        if (!journalEntries.isEmpty()) {
            addParagraph(document, "Nutrition notes were found in " + journalEntries.size()
                    + " journal day(s), so the food analysis below is based on the selected report range.");
        }
    }

    private void addCycleSummary(Document document, List<Cycle> cycles) {
        addSection(document, "Cycle summary");
        if (cycles.isEmpty()) {
            addEmptyPanel(document, "No period start was logged during this selected range.");
            return;
        }

        PdfPTable cards = table(3);
        Optional<Cycle> latest = cycles.stream().max(Comparator.comparing(Cycle::getLastPeriodStartDate));
        metricCell(cards, "Most recent start", latest.map(cycle -> cycle.getLastPeriodStartDate().toString()).orElse("Not recorded"));
        metricCell(cards, "Shortest cycle", cycleMetric(cycles.stream().map(Cycle::getActualCycleLength).filter(Objects::nonNull).min(Integer::compareTo)));
        metricCell(cards, "Longest cycle", cycleMetric(cycles.stream().map(Cycle::getActualCycleLength).filter(Objects::nonNull).max(Integer::compareTo)));
        safeAdd(document, cards);
    }

    private void addFlowSummary(Document document, List<FlowEntry> entries) {
        addSection(document, "Blood flow summary");
        if (entries.isEmpty()) {
            addEmptyPanel(document, "No blood flow entries were found in this selected range.");
            return;
        }

        flowService.mostCommonLevel(entries).ifPresent(level -> addParagraph(document,
                "Most common flow level: " + level.getLabel() + ". " + flowService.summary(entries)));
        addFlowNutrition(document, entries);
    }

    private void addWellnessSummary(Document document,
                                    List<WaterLog> waterLogs,
                                    List<SleepLog> sleepLogs,
                                    List<Mood> moods,
                                    List<Symptom> symptoms) {
        addSection(document, "Wellness pattern");
        PdfPTable cards = table(4);
        metricCell(cards, "Hydration", averageWater(waterLogs));
        metricCell(cards, "Sleep", averageSleep(sleepLogs));
        metricCell(cards, "Mood average", moods.isEmpty() ? "No data" : rounded(moods.stream().mapToInt(Mood::getIntensity).average().orElse(0)) + "/5");
        metricCell(cards, "Symptom load", symptoms.isEmpty() ? "None logged" : symptoms.size() + " log(s)");
        safeAdd(document, cards);
    }

    private void addNutritionSummary(Document document, List<JournalEntry> entries) {
        addSection(document, "Nutrition analysis");
        addNutritionAnalysis(document, entries);
    }

    private void addCompactDetails(Document document,
                                   List<Cycle> cycles,
                                   List<Symptom> symptoms,
                                   List<Mood> moods,
                                   List<WaterLog> waterLogs,
                                   List<SleepLog> sleepLogs,
                                   List<FlowEntry> flowEntries,
                                   List<JournalEntry> journalEntries) {
        addSection(document, "Detailed logs");
        addCompactTable(document, "Cycle starts", new String[]{"Start date", "Cycle length", "Duration"},
                cycles.stream().limit(8).map(cycle -> new String[]{
                        cycle.getLastPeriodStartDate().toString(),
                        cycle.getActualCycleLength() != null ? cycle.getActualCycleLength() + " days" : "Baseline",
                        cycle.getAveragePeriodDuration() + " days"
                }).toList());
        addCompactTable(document, "Symptoms", new String[]{"Date", "Symptom", "Severity"},
                symptoms.stream().limit(8).map(symptom -> new String[]{
                        symptom.getEntryDate().toString(),
                        symptom.getType().getLabel(),
                        symptom.getSeverity() + "/5"
                }).toList());
        addCompactTable(document, "Moods", new String[]{"Date", "Mood", "Intensity"},
                moods.stream().limit(8).map(mood -> new String[]{
                        mood.getEntryDate().toString(),
                        mood.getType().getLabel(),
                        mood.getIntensity() + "/5"
                }).toList());
        addCompactTable(document, "Wellness", new String[]{"Date", "Water", "Sleep"},
                wellnessRows(waterLogs, sleepLogs));
        addCompactTable(document, "Flow", new String[]{"Date", "Flow", "Color", "Clots"},
                flowEntries.stream().limit(8).map(entry -> new String[]{
                        entry.getEntryDate().toString(),
                        entry.getFlowLevel().getLabel(),
                        entry.getFlowColor().getLabel(),
                        entry.getClotSize().getLabel()
                }).toList());
        addCompactTable(document, "Journal", new String[]{"Date", "Nutrition", "Notes"},
                journalEntries.stream().limit(6).map(entry -> new String[]{
                        entry.getEntryDate().toString(),
                        compact(value(entry.getNutritionLog()), 95),
                        compact(value(entry.getObservations()), 70)
                }).toList());
    }

    private void addFooterNote(Document document) {
        addSection(document, "Important note");
        addParagraph(document, "CycleCare is an educational tracking tool and is not a medical diagnostic tool. Use this report to discuss patterns with a qualified clinician when needed.");
    }

    private void metricCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(11);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(metricBackground(label));
        Paragraph labelText = new Paragraph(label.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, MUTED));
        labelText.setSpacingAfter(4);
        Paragraph valueText = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, metricAccent(label)));
        cell.addElement(labelText);
        cell.addElement(valueText);
        table.addCell(cell);
    }

    private void insightCell(PdfPTable table, String title, String message) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(insightBackground(title));
        Paragraph titleText = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, insightAccent(title)));
        titleText.setSpacingAfter(5);
        cell.addElement(titleText);
        cell.addElement(new Paragraph(message, FontFactory.getFont(FontFactory.HELVETICA, 9, INK)));
        table.addCell(cell);
    }

    private void addEmptyPanel(Document document, String message) {
        PdfPTable panel = table(1);
        insightCell(panel, "No data in selected range", message);
        safeAdd(document, panel);
    }

    private void addCompactTable(Document document, String title, String[] headers, List<String[]> rows) {
        Paragraph paragraph = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PLUM));
        paragraph.setSpacingBefore(8);
        paragraph.setSpacingAfter(5);
        safeAdd(document, paragraph);

        if (rows.isEmpty()) {
            addParagraph(document, "No entries found.");
            return;
        }

        PdfPTable table = table(headers.length);
        header(table, headers);
        rows.forEach(values -> row(table, values));
        safeAdd(document, table);
    }

    private List<String[]> wellnessRows(List<WaterLog> waterLogs, List<SleepLog> sleepLogs) {
        Map<LocalDate, String> waterByDate = waterLogs.stream()
                .collect(LinkedHashMap::new,
                        (map, log) -> map.put(log.getEntryDate(), log.getAmountMl() + " ml"),
                        Map::putAll);
        Map<LocalDate, String> sleepByDate = sleepLogs.stream()
                .collect(LinkedHashMap::new,
                        (map, log) -> map.put(log.getEntryDate(), log.getHours() + "h " + log.getQuality().getLabel()),
                        Map::putAll);

        return java.util.stream.Stream.concat(waterByDate.keySet().stream(), sleepByDate.keySet().stream())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .limit(8)
                .map(date -> new String[]{
                        date.toString(),
                        waterByDate.getOrDefault(date, "Not recorded"),
                        sleepByDate.getOrDefault(date, "Not recorded")
                })
                .toList();
    }

    private String averageWater(List<WaterLog> logs) {
        if (logs.isEmpty()) {
            return "No data";
        }
        return rounded(logs.stream().mapToInt(WaterLog::getAmountMl).average().orElse(0)) + " ml";
    }

    private String averageSleep(List<SleepLog> logs) {
        if (logs.isEmpty()) {
            return "No data";
        }
        return rounded(logs.stream().mapToDouble(SleepLog::getHours).average().orElse(0)) + " h";
    }

    private String mostCommonSymptom(List<Symptom> symptoms) {
        return symptoms.stream()
                .collect(java.util.stream.Collectors.groupingBy(Symptom::getType, java.util.stream.Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().getLabel())
                .orElse("Symptoms");
    }

    private String cycleMetric(Optional<Integer> value) {
        return value.map(days -> days + " days").orElse("Not enough data");
    }

    private String compact(String value, int maxLength) {
        if (value == null || value.isBlank() || "Not recorded".equals(value)) {
            return "Not recorded";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength - 3) + "...";
    }

    private String rounded(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private Color metricAccent(String label) {
        String normalized = label.toLowerCase();
        if (normalized.contains("water") || normalized.contains("hydration") || normalized.contains("sleep")) {
            return TEAL;
        }
        if (normalized.contains("flow") || normalized.contains("period") || normalized.contains("cycle")) {
            return ROSE;
        }
        if (normalized.contains("mood") || normalized.contains("journal")) {
            return CORAL;
        }
        return PLUM;
    }

    private Color metricBackground(String label) {
        String normalized = label.toLowerCase();
        if (normalized.contains("water") || normalized.contains("hydration") || normalized.contains("sleep")) {
            return MINT;
        }
        if (normalized.contains("flow") || normalized.contains("period") || normalized.contains("cycle")) {
            return BLUSH;
        }
        return LAVENDER;
    }

    private Color insightAccent(String title) {
        String normalized = title.toLowerCase();
        if (normalized.contains("wellness")) {
            return TEAL;
        }
        if (normalized.contains("cycle") || normalized.contains("flow") || normalized.contains("symptom")) {
            return ROSE;
        }
        if (normalized.contains("mood")) {
            return CORAL;
        }
        return PLUM;
    }

    private Color insightBackground(String title) {
        String normalized = title.toLowerCase();
        if (normalized.contains("wellness")) {
            return MINT;
        }
        if (normalized.contains("cycle") || normalized.contains("flow") || normalized.contains("symptom")) {
            return BLUSH;
        }
        return LAVENDER;
    }

    private void addTitle(Document document, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PLUM);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(16);
        safeAdd(document, paragraph);
    }

    private void addSection(Document document, String text) {
        PdfPTable section = new PdfPTable(1);
        section.setWidthPercentage(100);
        section.setSpacingBefore(14);
        section.setSpacingAfter(8);
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        cell.setPadding(8);
        cell.setBorderColor(PLUM);
        cell.setBackgroundColor(PLUM);
        section.addCell(cell);
        safeAdd(document, section);
    }

    private void addParagraph(Document document, String text) {
        Paragraph paragraph = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 10, INK));
        paragraph.setSpacingAfter(8);
        safeAdd(document, paragraph);
    }

    private void addSubsection(Document document, String text) {
        PdfPTable section = new PdfPTable(1);
        section.setWidthPercentage(100);
        section.setSpacingBefore(10);
        section.setSpacingAfter(6);
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PLUM)));
        cell.setPadding(8);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(LAVENDER);
        section.addCell(cell);
        safeAdd(document, section);
    }

    private void addCycles(Document document, List<Cycle> cycles) {
        addSection(document, "Cycle History");
        PdfPTable table = table(4);
        header(table, "Start date", "Cycle length", "Period duration", "Notes");
        cycles.stream().limit(12).forEach(cycle -> row(table,
                cycle.getLastPeriodStartDate().toString(),
                (cycle.getActualCycleLength() != null ? cycle.getActualCycleLength() + " days" : "Baseline"),
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
        addSubsection(document, "Flow-Based Nutrition Recommendations");
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

        addSubsection(document, "AI Nutrition Analysis");

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

        addSubsection(document, "Healthy Choices");

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

        addSubsection(document, "Foods To Limit");

        if (combined.getFoodsToLimit().isEmpty()) {

            addParagraph(
                    document,
                    "Excellent. No foods to limit were detected in this selected range."
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

        addSubsection(document, "AI Wellness Insight");

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
            PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
            cell.setPadding(7);
            cell.setBorderColor(PLUM);
            cell.setBackgroundColor(PLUM);
            table.addCell(cell);
        }
    }

    private void row(PdfPTable table, String... values) {
        for (String value : values) {
            table.addCell(cell(value));
        }
    }

    private PdfPCell cell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, FontFactory.getFont(FontFactory.HELVETICA, 8, INK)));
        cell.setPadding(6);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(TABLE_STRIPE);
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
