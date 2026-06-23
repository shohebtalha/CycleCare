package com.cyclecare.service;

import com.cyclecare.domain.MenstrualPhase;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AssistantService {

    public String answer(String question, MenstrualPhase phase) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT);
        String disclaimer = " This is educational information, not a diagnosis. Please consult a qualified clinician for severe, unusual, or persistent symptoms.";

        if (containsAny(normalized, "diagnose", "pregnant", "pregnancy", "infection", "pcos", "endometriosis")) {
            return "CycleCare cannot diagnose medical conditions or confirm pregnancy. Track your symptoms and seek professional care for testing, diagnosis, or treatment." + disclaimer;
        }
        if (containsAny(normalized, "cramp", "pain", "back pain")) {
            return "For mild cramps or back discomfort, gentle heat, hydration, rest, stretching, and light walking may help. Severe pain, sudden pain, fever, fainting, or pain that disrupts daily life should be checked by a clinician." + disclaimer;
        }
        if (containsAny(normalized, "food", "nutrition", "eat", "diet", "iron", "magnesium")) {
            return switch (phase) {
                case MENSTRUAL -> "During the menstrual phase, consider iron-rich foods like lentils, beans, spinach, eggs, tofu, and vitamin C-rich fruit. Warm fluids and regular hydration may support comfort." + disclaimer;
                case FOLLICULAR -> "During the follicular phase, lean proteins, fresh fruits, vegetables, fermented foods, and whole grains can support rising energy." + disclaimer;
                case OVULATION -> "Around ovulation, antioxidant-rich fruits and vegetables, fiber-rich foods, and steady hydration are helpful general wellness choices." + disclaimer;
                case LUTEAL -> "During the luteal phase, magnesium-rich foods, complex carbohydrates, yogurt, nuts, seeds, and planned snacks may help with cravings and PMS comfort." + disclaimer;
            };
        }
        if (containsAny(normalized, "exercise", "workout", "yoga", "cardio", "strength")) {
            return switch (phase) {
                case MENSTRUAL -> "Menstrual phase exercise can be gentle: walking, restorative yoga, stretching, and rest as needed.";
                case FOLLICULAR -> "Follicular phase may be a good time for strength training, moderate cardio, and progressive workouts if energy feels good.";
                case OVULATION -> "Ovulation can suit moderate cardio or strength work, with warm-up, hydration, and attention to comfort.";
                case LUTEAL -> "Luteal phase movement often works best when steady and calming: walking, yoga, stretching, and lighter strength sessions.";
            } + disclaimer;
        }
        if (containsAny(normalized, "late", "missed", "irregular", "gap")) {
            return "Late or irregular periods can happen for many reasons including stress, sleep changes, nutrition, training load, illness, pregnancy, or health conditions. Track dates, note symptoms, and seek professional advice for repeated irregularity or long gaps." + disclaimer;
        }
        if (containsAny(normalized, "mood", "sad", "anxious", "stress", "irritated")) {
            return "Mood changes can fluctuate across the cycle. Gentle movement, sleep consistency, hydration, journaling, and balanced meals may help. If sadness, anxiety, or stress feels intense or unsafe, please contact a mental health professional or local emergency support." + disclaimer;
        }
        return "I can explain cycle phases, symptoms, nutrition, exercise, tracking patterns, and when to consider professional consultation. Ask about a symptom, food idea, workout choice, or cycle pattern." + disclaimer;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
