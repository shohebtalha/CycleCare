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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents",
        indexes = {
                @Index(name = "idx_user_consents_user_version", columnList = "user_id,consent_version"),
                @Index(name = "idx_user_consents_accepted_at", columnList = "accepted_at")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_consents_user_version",
                columnNames = {"user_id", "consent_version"}))
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "consent_version", nullable = false, length = 40)
    private String consentVersion;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "accepted_privacy_policy", nullable = false)
    private boolean acceptedPrivacyPolicy;

    @Column(name = "accepted_terms", nullable = false)
    private boolean acceptedTerms;

    @PrePersist
    void onCreate() {
        if (acceptedAt == null) {
            acceptedAt = LocalDateTime.now();
        }
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

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String consentVersion) {
        this.consentVersion = consentVersion;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

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
