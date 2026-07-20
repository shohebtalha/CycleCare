package com.cyclecare.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public class AccountDeletionDto {

    @NotBlank(message = "Password confirmation is required")
    private String password;

    @AssertTrue(message = "Please confirm that you understand deletion is irreversible")
    private boolean irreversibleAcknowledged;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIrreversibleAcknowledged() {
        return irreversibleAcknowledged;
    }

    public void setIrreversibleAcknowledged(boolean irreversibleAcknowledged) {
        this.irreversibleAcknowledged = irreversibleAcknowledged;
    }
}
