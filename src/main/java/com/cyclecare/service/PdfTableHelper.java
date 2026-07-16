package com.cyclecare.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;

/**
 * Utility for generating common PDF layout elements (tables, headers, cells, typography).
 */
public final class PdfTableHelper {

    private PdfTableHelper() {}

    public static PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        return table;
    }

    public static void header(PdfPTable table, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
            cell.setPadding(7);
            cell.setBorderColor(PdfColorTheme.PLUM);
            cell.setBackgroundColor(PdfColorTheme.PLUM);
            table.addCell(cell);
        }
    }

    public static void row(PdfPTable table, String... values) {
        for (String value : values) {
            table.addCell(cell(value));
        }
    }

    public static PdfPCell cell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, FontFactory.getFont(FontFactory.HELVETICA, 8, PdfColorTheme.INK)));
        cell.setPadding(6);
        cell.setBorderColor(PdfColorTheme.BORDER);
        cell.setBackgroundColor(PdfColorTheme.TABLE_STRIPE);
        return cell;
    }

    public static void metricCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(11);
        cell.setBorderColor(PdfColorTheme.BORDER);
        cell.setBackgroundColor(PdfColorTheme.metricBackground(label));
        Paragraph labelText = new Paragraph(label.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, PdfColorTheme.MUTED));
        labelText.setSpacingAfter(4);
        Paragraph valueText = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PdfColorTheme.metricAccent(label)));
        cell.addElement(labelText);
        cell.addElement(valueText);
        table.addCell(cell);
    }

    public static void insightCell(PdfPTable table, String title, String message) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(PdfColorTheme.BORDER);
        cell.setBackgroundColor(PdfColorTheme.insightBackground(title));
        Paragraph titleText = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PdfColorTheme.insightAccent(title)));
        titleText.setSpacingAfter(5);
        cell.addElement(titleText);
        cell.addElement(new Paragraph(message, FontFactory.getFont(FontFactory.HELVETICA, 9, PdfColorTheme.INK)));
        table.addCell(cell);
    }

    public static void addSection(Document document, String text) {
        PdfPTable section = new PdfPTable(1);
        section.setWidthPercentage(100);
        section.setSpacingBefore(14);
        section.setSpacingAfter(8);
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        cell.setPadding(8);
        cell.setBorderColor(PdfColorTheme.PLUM);
        cell.setBackgroundColor(PdfColorTheme.PLUM);
        section.addCell(cell);
        safeAdd(document, section);
    }

    public static void addSubsection(Document document, String text) {
        PdfPTable section = new PdfPTable(1);
        section.setWidthPercentage(100);
        section.setSpacingBefore(10);
        section.setSpacingAfter(6);
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PdfColorTheme.PLUM)));
        cell.setPadding(8);
        cell.setBorderColor(PdfColorTheme.BORDER);
        cell.setBackgroundColor(PdfColorTheme.LAVENDER);
        section.addCell(cell);
        safeAdd(document, section);
    }

    public static void addParagraph(Document document, String text) {
        Paragraph paragraph = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 10, PdfColorTheme.INK));
        paragraph.setSpacingAfter(8);
        safeAdd(document, paragraph);
    }

    public static void addTitle(Document document, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PdfColorTheme.PLUM);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(16);
        safeAdd(document, paragraph);
    }

    public static void safeAdd(Document document, Element element) {
        try {
            document.add(element);
        } catch (DocumentException ex) {
            throw new IllegalStateException("Unable to write PDF content.", ex);
        }
    }

    public static String value(Object value) {
        return value == null || value.toString().isBlank() ? "Not recorded" : value.toString();
    }
}
