# Step-by-Step Implementation Plan

1. Create Maven Spring Boot project with web, Thymeleaf, security, validation, JPA, MySQL, and PDF dependencies.
2. Configure MySQL datasource, Hibernate, sessions, Thymeleaf, and security properties.
3. Build domain entities and enums for users, cycles, tracking logs, recommendations, and insights.
4. Create repositories with user-scoped query methods.
5. Add DTOs with validation for registration, profile, cycle input, daily logs, journal, and assistant questions.
6. Implement Spring Security with custom user details, BCrypt password encoder, login/logout, CSRF, and session protection.
7. Implement user registration, profile update, forgot password, and reset password services.
8. Implement cycle calculation for current day, next period, ovulation, fertility window, and phase.
9. Seed phase-based nutrition and exercise recommendations.
10. Implement tracking services for symptoms, moods, water, sleep, and journal.
11. Implement analytics for cycle trends, mood trend, symptom frequency, period regularity, insights, and alerts.
12. Implement rule-based assistant responses with diagnosis avoidance and consultation guidance.
13. Implement calendar event generation.
14. Implement PDF export.
15. Build Thymeleaf templates with Bootstrap, Chart.js, responsive layout, dark mode, and validation errors.
16. Add REST endpoints where JSON responses are useful.
17. Verify Maven build and run against MySQL.

## Suggested Testing Strategy

- Unit test cycle calculation edge cases.
- Unit test assistant keyword categories.
- Repository tests with Testcontainers MySQL.
- MVC tests for auth redirects and protected pages.
- Service tests for user-scoped access.
- Manual UI test for registration, login, cycle setup, logs, dashboard, charts, PDF export.
