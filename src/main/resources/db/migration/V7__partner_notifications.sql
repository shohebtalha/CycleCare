ALTER TABLE app_users ADD COLUMN partner_email VARCHAR(160) NULL;
ALTER TABLE app_users ADD COLUMN partner_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE partner_notification_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    partner_email VARCHAR(160) NOT NULL,
    predicted_period_date DATE NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    sent_at DATETIME NOT NULL,
    CONSTRAINT fk_partner_notification_logs_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_partner_notification_cycle UNIQUE (user_id, partner_email, predicted_period_date, notification_type),
    INDEX idx_partner_notification_logs_user (user_id),
    INDEX idx_partner_notification_logs_date (predicted_period_date)
);
