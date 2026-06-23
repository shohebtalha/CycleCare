package com.cyclecare.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationDto {

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must be under 120 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @AssertTrue(message = "Passwords must match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
