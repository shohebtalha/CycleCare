ALTER TABLE app_users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE app_users
SET enabled = email_verified;

CREATE TABLE email_verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    expiry_time DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_email_verification_tokens_user (user_id),
    INDEX idx_email_verification_tokens_token (token)
);
