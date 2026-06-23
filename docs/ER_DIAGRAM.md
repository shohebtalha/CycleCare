# ER Diagram Description

## Mermaid ER Diagram

```mermaid
erDiagram
    APP_USERS ||--o{ CYCLES : owns
    APP_USERS ||--o{ SYMPTOMS : logs
    APP_USERS ||--o{ MOODS : logs
    APP_USERS ||--o{ WATER_LOGS : logs
    APP_USERS ||--o{ SLEEP_LOGS : logs
    APP_USERS ||--o{ JOURNAL_ENTRIES : writes
    APP_USERS ||--o{ HEALTH_INSIGHTS : receives
    NUTRITION_RECOMMENDATIONS }o--|| MENSTRUAL_PHASE : references
    EXERCISE_RECOMMENDATIONS }o--|| MENSTRUAL_PHASE : references

    APP_USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar name
        int age
        double height
        double weight
        varchar activity_level
        varchar role
        varchar reset_token
        datetime reset_token_expires_at
    }

    CYCLES {
        bigint id PK
        bigint user_id FK
        date last_period_start_date
        int average_cycle_length
        int average_period_duration
        varchar notes
    }

    SYMPTOMS {
        bigint id PK
        bigint user_id FK
        date entry_date
        varchar type
        varchar custom_symptom
        int severity
        varchar notes
    }

    MOODS {
        bigint id PK
        bigint user_id FK
        date entry_date
        varchar type
        int intensity
        varchar notes
    }

    WATER_LOGS {
        bigint id PK
        bigint user_id FK
        date entry_date
        int amount_ml
    }

    SLEEP_LOGS {
        bigint id PK
        bigint user_id FK
        date entry_date
        double hours
        varchar quality
        varchar notes
    }

    JOURNAL_ENTRIES {
        bigint id PK
        bigint user_id FK
        date entry_date
        varchar physical_symptoms
        varchar emotional_state
        varchar observations
    }

    HEALTH_INSIGHTS {
        bigint id PK
        bigint user_id FK
        date insight_date
        varchar title
        varchar message
        varchar type
    }
```

## Relationship Summary

- One user owns many cycles, symptoms, moods, water logs, sleep logs, journal entries, and health insights.
- Nutrition and exercise recommendations are phase-based reference data.
- All user-owned queries filter by authenticated user to prevent cross-user access.
