package com.cyclecare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AssistantQuestionDto {

    @NotBlank(message = "Ask a question to continue")
    @Size(max = 500, message = "Questions must be under 500 characters")
    private String question;

    private String answer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
