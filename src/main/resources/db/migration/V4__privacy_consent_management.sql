CREATE TABLE user_consents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    consent_version VARCHAR(40) NOT NULL,
    accepted_at DATETIME NOT NULL,
    accepted_privacy_policy BOOLEAN NOT NULL,
    accepted_terms BOOLEAN NOT NULL,
    CONSTRAINT fk_user_consents_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_consents_user_version UNIQUE (user_id, consent_version),
    INDEX idx_user_consents_user_version (user_id, consent_version),
    INDEX idx_user_consents_accepted_at (accepted_at)
);
