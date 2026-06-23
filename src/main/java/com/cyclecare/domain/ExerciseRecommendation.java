package com.cyclecare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_recommendations")
public class ExerciseRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MenstrualPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExerciseType exerciseType;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 800)
    private String description;

    @Column(nullable = false, length = 40)
    private String intensity;

    @Column(nullable = false)
    private Integer durationMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MenstrualPhase getPhase() {
        return phase;
    }

    public void setPhase(MenstrualPhase phase) {
        this.phase = phase;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
