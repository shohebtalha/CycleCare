package com.cyclecare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_notification_logs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_partner_notification_cycle",
                columnNames = {"user_id", "partner_email", "predicted_period_date", "notification_type"}))
public class PartnerNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 160)
    private String partnerEmail;

    @Column(nullable = false)
    private LocalDate predictedPeriodDate;

    @Column(nullable = false, length = 40)
    private String notificationType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    void onCreate() {
        sentAt = LocalDateTime.now();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPartnerEmail(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }

    public void setPredictedPeriodDate(LocalDate predictedPeriodDate) {
        this.predictedPeriodDate = predictedPeriodDate;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}
