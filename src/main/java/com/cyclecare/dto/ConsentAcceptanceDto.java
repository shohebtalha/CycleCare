package com.cyclecare.dto;

import jakarta.validation.constraints.AssertTrue;

public class ConsentAcceptanceDto {

    @AssertTrue(message = "You must accept the current Privacy Policy")
    private boolean acceptedPrivacyPolicy;

    @AssertTrue(message = "You must accept the current Terms & Conditions")
    private boolean acceptedTerms;

    public boolean isAcceptedPrivacyPolicy() {
        return acceptedPrivacyPolicy;
    }

    public void setAcceptedPrivacyPolicy(boolean acceptedPrivacyPolicy) {
        this.acceptedPrivacyPolicy = acceptedPrivacyPolicy;
    }

    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }

    public void setAcceptedTerms(boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }
}
