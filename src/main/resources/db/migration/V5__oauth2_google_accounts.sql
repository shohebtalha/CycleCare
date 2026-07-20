ALTER TABLE app_users ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE app_users ADD COLUMN provider_user_id VARCHAR(120) NULL;
ALTER TABLE app_users ADD COLUMN avatar_url VARCHAR(500) NULL;
ALTER TABLE app_users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE app_users
SET email_verified = TRUE
WHERE auth_provider = 'LOCAL';

CREATE INDEX idx_app_users_provider_user ON app_users (auth_provider, provider_user_id);
