# CycleCare Architecture

## High-Level Architecture

CycleCare follows a layered Spring Boot MVC architecture:

```text
Browser
  -> Thymeleaf MVC Controllers
  -> Service Layer
  -> Repository Layer
  -> MySQL
```

REST endpoints are provided under `/api` for dashboard, cycle, analytics, calendar, recommendations, and water logging.

## Layers

### Domain Layer

Entities:

- `User`
- `Cycle`
- `Symptom`
- `Mood`
- `NutritionRecommendation`
- `ExerciseRecommendation`
- `WaterLog`
- `SleepLog`
- `JournalEntry`
- `HealthInsight`

Enums model controlled values such as menstrual phase, activity level, symptom type, mood type, sleep quality, and exercise type.

### DTO Layer

Form inputs use DTOs with Jakarta Bean Validation annotations. This keeps validation separate from JPA entities and avoids accidentally binding sensitive entity fields.

### Repository Layer

Spring Data JPA repositories expose user-scoped queries such as:

- latest cycle by user
- symptom history by user
- mood history by user
- water totals by date
- sleep averages by date range

### Service Layer

Business logic lives in services:

- `UserService`: registration, profile updates, password reset tokens.
- `CycleService` and `CycleCalculator`: cycle prediction logic.
- `RecommendationService`: phase-based nutrition and exercise retrieval.
- `SymptomService`, `MoodService`, `WaterService`, `SleepService`, `JournalService`: tracking workflows.
- `AnalyticsService`: trends, regularity score, health insights, alerts.
- `AssistantService`: rule-based educational answers.
- `CalendarService`: month event markers.
- `ReportService`: PDF export.

### Controller Layer

MVC controllers render Thymeleaf templates. `ApiController` exposes JSON endpoints for selected REST use cases.

### Security

Spring Security provides:

- form login
- session-based authentication
- BCrypt password hashing
- CSRF protection
- remember-me option
- route protection
- user-specific data access through service methods

## Folder Structure

```text
src/main/java/com/cyclecare
  CycleCareApplication.java
  config/
  controller/
  domain/
  dto/
  repository/
  security/
  service/
src/main/resources
  application.properties
  static/css/app.css
  static/js/app.js
  templates/
```

## Production Notes

- Replace the development password reset display with an email provider.
- Set `spring.jpa.hibernate.ddl-auto=validate` in production.
- Move secrets into environment variables.
- Add database migrations with Flyway or Liquibase.
- Add audit logging and rate limiting for auth endpoints.
