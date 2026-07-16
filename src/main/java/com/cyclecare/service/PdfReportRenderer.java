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

import static com.cyclecare.service.PdfColorTheme.*;
import static com.cyclecare.service.PdfTableHelper.*;

@Service
public class PdfReportRenderer {

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

    private void addEmptyPanel(Document document, String message) {
        PdfPTable panel = PdfTableHelper.table(1);
        PdfTableHelper.insightCell(panel, "No data in selected range", message);
        PdfTableHelper.safeAdd(document, panel);
    }

    private void addCompactTable(Document document, String title, String[] headers, List<String[]> rows) {
        Paragraph paragraph = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PdfColorTheme.PLUM));
        paragraph.setSpacingBefore(8);
        paragraph.setSpacingAfter(5);
        PdfTableHelper.safeAdd(document, paragraph);

        if (rows.isEmpty()) {
            PdfTableHelper.addParagraph(document, "No entries found.");
            return;
        }

        PdfPTable table = PdfTableHelper.table(headers.length);
        PdfTableHelper.header(table, headers);
        rows.forEach(values -> PdfTableHelper.row(table, values));
        PdfTableHelper.safeAdd(document, table);
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

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "Not recorded" : value.toString();
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

    private void addNutritionAnalysis(Document document, List<JournalEntry> entries) {
        addSubsection(document, "AI Nutrition Analysis");
        NutritionAnalysis combined = new NutritionAnalysis();
        Map<String, FoodInfo> healthyFoods = new LinkedHashMap<>();
        Map<String, FoodInfo> limitFoods = new LinkedHashMap<>();

        for (JournalEntry entry : entries) {
            if (entry.getNutritionLog() == null || entry.getNutritionLog().isBlank()) continue;
            NutritionAnalysis analysis = nutritionAnalyzerService.analyze(entry.getNutritionLog());
            for (FoodInfo food : analysis.getHealthyFoods()) healthyFoods.putIfAbsent(food.getName().toLowerCase(), food);
            for (FoodInfo food : analysis.getFoodsToLimit()) limitFoods.putIfAbsent(food.getName().toLowerCase(), food);
            combined.getUnknownFoods().addAll(analysis.getUnknownFoods());
        }

        combined.getHealthyFoods().addAll(healthyFoods.values());
        combined.getFoodsToLimit().addAll(limitFoods.values());

        if (combined.getHealthyFoods().isEmpty() && combined.getFoodsToLimit().isEmpty()) {
            addParagraph(document, "No nutrition records available.");
            return;
        }

        addSubsection(document, "Healthy Choices");
        com.lowagie.text.pdf.PdfPTable healthyTable = table(3);
        header(healthyTable, "Food", "Health Benefit", "Cycle Benefit");
        for (FoodInfo food : combined.getHealthyFoods()) {
            row(healthyTable, food.getName(), value(food.getBenefit()), value(food.getPeriodBenefit()));
        }
        safeAdd(document, healthyTable);

        addSubsection(document, "Foods To Limit");
        if (combined.getFoodsToLimit().isEmpty()) {
            addParagraph(document, "Excellent. No foods to limit were detected in this selected range.");
        } else {
            com.lowagie.text.pdf.PdfPTable limitTable = table(2);
            header(limitTable, "Food", "Reason");
            for (FoodInfo food : combined.getFoodsToLimit()) {
                row(limitTable, food.getName(), value(food.getWarning()));
            }
            safeAdd(document, limitTable);
        }
        String aiSummary = assistantService.generateNutritionReport(combined, nutritionPromptBuilder);
        addSubsection(document, "AI Wellness Insight");
        addParagraph(document, aiSummary);
    }
}
