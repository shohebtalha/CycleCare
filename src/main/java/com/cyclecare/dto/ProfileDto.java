package com.cyclecare.dto;

import com.cyclecare.domain.ActivityLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProfileDto {

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must be under 120 characters")
    private String name;

    @Min(value = 10, message = "Age must be at least 10")
    @Max(value = 80, message = "Age must be realistic")
    private Integer age;

    @DecimalMin(value = "100.0", message = "Height must be at least 100 cm")
    @DecimalMax(value = "240.0", message = "Height must be under 240 cm")
    private Double height;

    @DecimalMin(value = "25.0", message = "Weight must be at least 25 kg")
    @DecimalMax(value = "250.0", message = "Weight must be under 250 kg")
    private Double weight;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }
}
