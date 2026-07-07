ALTER TABLE cycles
    ADD COLUMN actual_cycle_length INT NULL;

CREATE TABLE cycle_prediction_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    predicted_period_date DATE NOT NULL,
    actual_period_start_date DATE NOT NULL,
    predicted_cycle_length INT NOT NULL,
    actual_cycle_length INT NOT NULL,
    prediction_error_days INT NOT NULL,
    confidence VARCHAR(20) NOT NULL,
    explanation VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_cycle_prediction_history_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_cycle_prediction_history_user_created (user_id, created_at),
    INDEX idx_cycle_prediction_history_user_actual (user_id, actual_period_start_date)
);
