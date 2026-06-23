package com.cyclecare.service;

import com.cyclecare.domain.Cycle;
import com.cyclecare.domain.JournalEntry;
import com.cyclecare.domain.Mood;
import com.cyclecare.domain.SleepLog;
import com.cyclecare.domain.Symptom;
import com.cyclecare.domain.User;
import com.cyclecare.domain.WaterLog;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

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

    public ReportService(CycleService cycleService,
                         SymptomService symptomService,
                         MoodService moodService,
                         WaterService waterService,
                         SleepService sleepService,
                         JournalService journalService,
                         AnalyticsService analyticsService) {
        this.cycleService = cycleService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.journalService = journalService;
        this.analyticsService = analyticsService;
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
            addJournal(document, journalService.history(user));

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

    private void addJournal(Document document, List<JournalEntry> entries) {
        addSection(document, "Journal Highlights");
        entries.stream().limit(5).forEach(entry -> addParagraph(document,
                entry.getEntryDate() + " - Physical: " + value(entry.getPhysicalSymptoms())
                        + " | Emotional: " + value(entry.getEmotionalState())
                        + " | Notes: " + value(entry.getObservations())));
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
