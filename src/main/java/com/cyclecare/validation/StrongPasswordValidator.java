package com.cyclecare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() < MIN_LENGTH) {
            return false;
        }
        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isLowerCase(character)) {
                hasLowercase = true;
            } else if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isDigit(character)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasLowercase && hasUppercase && hasDigit && hasSpecial;
    }
}
