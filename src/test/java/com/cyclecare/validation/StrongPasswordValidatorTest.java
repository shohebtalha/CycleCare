package com.cyclecare.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void acceptsPasswordWithLowercaseUppercaseNumberSpecialAndMinimumLength() {
        assertThat(validator.isValid("Cycle@123", null)).isTrue();
    }

    @Test
    void rejectsWeakPasswords() {
        assertThat(validator.isValid("cycle@123", null)).isFalse();
        assertThat(validator.isValid("CYCLE@123", null)).isFalse();
        assertThat(validator.isValid("Cycleabc", null)).isFalse();
        assertThat(validator.isValid("Cycle123", null)).isFalse();
        assertThat(validator.isValid("Cy@123", null)).isFalse();
    }
}
