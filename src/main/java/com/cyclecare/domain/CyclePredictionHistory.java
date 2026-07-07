package com.cyclecare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_prediction_history", indexes = {
        @Index(name = "idx_cycle_prediction_history_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_cycle_prediction_history_user_actual", columnList = "user_id,actual_period_start_date")
})
public class CyclePredictionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate predictedPeriodDate;

    @Column(nullable = false)
    private LocalDate actualPeriodStartDate;

    @Column(nullable = false)
    private Integer predictedCycleLength;

    @Column(nullable = false)
    private Integer actualCycleLength;

    @Column(nullable = false)
    private Integer predictionErrorDays;

    @Column(nullable = false, length = 20)
    private String confidence;

    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getPredictedPeriodDate() {
        return predictedPeriodDate;
    }

    public void setPredictedPeriodDate(LocalDate predictedPeriodDate) {
        this.predictedPeriodDate = predictedPeriodDate;
    }

    public LocalDate getActualPeriodStartDate() {
        return actualPeriodStartDate;
    }

    public void setActualPeriodStartDate(LocalDate actualPeriodStartDate) {
        this.actualPeriodStartDate = actualPeriodStartDate;
    }

    public Integer getPredictedCycleLength() {
        return predictedCycleLength;
    }

    public void setPredictedCycleLength(Integer predictedCycleLength) {
        this.predictedCycleLength = predictedCycleLength;
    }

    public Integer getActualCycleLength() {
        return actualCycleLength;
    }

    public void setActualCycleLength(Integer actualCycleLength) {
        this.actualCycleLength = actualCycleLength;
    }

    public Integer getPredictionErrorDays() {
        return predictionErrorDays;
    }

    public void setPredictionErrorDays(Integer predictionErrorDays) {
        this.predictionErrorDays = predictionErrorDays;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
