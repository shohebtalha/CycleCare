CREATE DATABASE IF NOT EXISTS cyclecare;
USE cyclecare;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(120) NOT NULL,
    age INT NULL,
    height DOUBLE NULL,
    weight DOUBLE NULL,
    activity_level VARCHAR(20) NOT NULL DEFAULT 'MODERATE',
    role VARCHAR(40) NOT NULL DEFAULT 'ROLE_USER',
    reset_token VARCHAR(120) NULL,
    reset_token_expires_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS cycles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    last_period_start_date DATE NOT NULL,
    average_cycle_length INT NOT NULL,
    average_period_duration INT NOT NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_cycles_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_cycles_user_start (user_id, last_period_start_date)
);

CREATE TABLE IF NOT EXISTS symptoms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    type VARCHAR(40) NOT NULL,
    custom_symptom VARCHAR(120) NULL,
    severity INT NOT NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_symptoms_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_symptoms_user_date (user_id, entry_date),
    INDEX idx_symptoms_user_severity (user_id, severity)
);

CREATE TABLE IF NOT EXISTS moods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    type VARCHAR(40) NOT NULL,
    intensity INT NOT NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_moods_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_moods_user_date (user_id, entry_date)
);

CREATE TABLE IF NOT EXISTS nutrition_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phase VARCHAR(40) NOT NULL UNIQUE,
    recommended_foods VARCHAR(900) NOT NULL,
    foods_to_avoid VARCHAR(700) NOT NULL,
    daily_tips VARCHAR(900) NOT NULL,
    explanation VARCHAR(1200) NOT NULL
);

CREATE TABLE IF NOT EXISTS exercise_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phase VARCHAR(40) NOT NULL,
    exercise_type VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(800) NOT NULL,
    intensity VARCHAR(40) NOT NULL,
    duration_minutes INT NOT NULL,
    INDEX idx_exercise_phase (phase)
);

CREATE TABLE IF NOT EXISTS water_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    amount_ml INT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_water_logs_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_water_user_date (user_id, entry_date)
);

CREATE TABLE IF NOT EXISTS sleep_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    hours DOUBLE NOT NULL,
    quality VARCHAR(30) NOT NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_sleep_logs_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_sleep_user_date (user_id, entry_date)
);

CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    physical_symptoms VARCHAR(1200) NULL,
    emotional_state VARCHAR(1200) NULL,
    observations VARCHAR(2000) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_journal_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_journal_user_date (user_id, entry_date)
);

CREATE TABLE IF NOT EXISTS health_insights (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    insight_date DATE NOT NULL,
    title VARCHAR(140) NOT NULL,
    message VARCHAR(1200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_health_insights_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    INDEX idx_health_insights_user_date (user_id, insight_date)
);
