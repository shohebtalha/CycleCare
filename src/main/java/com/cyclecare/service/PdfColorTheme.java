package com.cyclecare.service;

import java.awt.Color;

/**
 * Centralized color palette and label-based color dispatch for PDF report rendering.
 * All methods and constants are static — this is a pure utility, not a Spring bean.
 */
public final class PdfColorTheme {

    public static final Color INK          = new Color(54,  32,  45);
    public static final Color MUTED        = new Color(111, 91,  103);
    public static final Color PLUM         = new Color(86,  48,  91);
    public static final Color ROSE         = new Color(224, 74,  112);
    public static final Color CORAL        = new Color(246, 125, 101);
    public static final Color TEAL         = new Color(49,  151, 149);
    public static final Color LAVENDER     = new Color(245, 239, 249);
    public static final Color BLUSH        = new Color(255, 241, 245);
    public static final Color MINT         = new Color(235, 250, 248);
    public static final Color BORDER       = new Color(226, 213, 226);
    public static final Color TABLE_STRIPE = new Color(252, 248, 251);

    private PdfColorTheme() {}

    public static Color metricAccent(String label) {
        String n = label.toLowerCase();
        if (n.contains("water") || n.contains("hydration") || n.contains("sleep")) return TEAL;
        if (n.contains("flow")  || n.contains("period")    || n.contains("cycle"))  return ROSE;
        if (n.contains("mood")  || n.contains("journal"))                            return CORAL;
        return PLUM;
    }

    public static Color metricBackground(String label) {
        String n = label.toLowerCase();
        if (n.contains("water") || n.contains("hydration") || n.contains("sleep")) return MINT;
        if (n.contains("flow")  || n.contains("period")    || n.contains("cycle"))  return BLUSH;
        return LAVENDER;
    }

    public static Color insightAccent(String title) {
        String n = title.toLowerCase();
        if (n.contains("wellness"))                                                  return TEAL;
        if (n.contains("cycle") || n.contains("flow") || n.contains("symptom"))     return ROSE;
        if (n.contains("mood"))                                                      return CORAL;
        return PLUM;
    }

    public static Color insightBackground(String title) {
        String n = title.toLowerCase();
        if (n.contains("wellness"))                                                  return MINT;
        if (n.contains("cycle") || n.contains("flow") || n.contains("symptom"))     return BLUSH;
        return LAVENDER;
    }
}
