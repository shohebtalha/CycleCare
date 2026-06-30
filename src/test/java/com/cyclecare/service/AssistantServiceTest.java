//package com.cyclecare.service;
//
//import com.cyclecare.domain.MenstrualPhase;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class AssistantServiceTest {
//
//    private final AssistantService assistantService = new AssistantService();
//
//    @Test
//    void avoidsDiagnosisForMedicalConditionQuestions() {
//        String answer = assistantService.answer("Can you diagnose PCOS?", MenstrualPhase.LUTEAL);
//
//        assertThat(answer).contains("cannot diagnose");
//        assertThat(answer).contains("clinician");
//    }
//
//    @Test
//    void givesPhaseBasedNutritionTips() {
//        String answer = assistantService.answer("What should I eat?", MenstrualPhase.MENSTRUAL);
//
//        assertThat(answer).contains("iron-rich");
//        assertThat(answer).contains("educational");
//    }
//}
