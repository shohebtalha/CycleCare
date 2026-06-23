# CycleCare

CycleCare is a full-stack Spring Boot resume project for menstrual health tracking. It uses Spring Boot, Thymeleaf, Spring Security, BCrypt, JPA/Hibernate, MySQL, Bootstrap, Chart.js, Maven, and selected REST endpoints.

## Features

- Registration, login, session authentication, BCrypt password hashing, remember-me, forgot/reset password token flow.
- Profile management with name, age, height, weight, and activity level.
- Cycle prediction for current cycle day, next period, ovulation, fertility window, and menstrual phase.
- Dashboard with cycle cards, symptoms, moods, water, sleep, health insights, nutrition, and exercise guidance.
- Symptom, mood, water, sleep, and daily journal tracking.
- Phase-based nutrition and exercise recommendation engines.
- Calendar markers for period days, predicted periods, ovulation, fertility window, and logged symptoms.
- Analytics with Chart.js for cycle trend, mood trend, symptom frequency, and regularity score.
- Rule-based educational health assistant with safety disclaimer.
- Health alerts for irregular cycles, repeated severe symptoms, and long gaps.
- PDF health report export.
- Light and dark mode responsive Bootstrap UI.

## Tech Stack

- Java 17
- Spring Boot 3.3.5
- Spring MVC and Thymeleaf
- Spring Security
- Spring Data JPA and Hibernate
- MySQL
- Maven
- Bootstrap 5
- Chart.js
- OpenPDF

## Quick Start

1. Start MySQL with Docker Compose:

```bash
docker compose up -d
```

Or create a local MySQL database manually:

```sql
CREATE DATABASE cyclecare;
```

2. If you are not using the included Compose file, update `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

3. Run the app:

```bash
mvn spring-boot:run
```

4. Open:

```text
http://localhost:8080
```

Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update` for local development. The explicit MySQL schema is available in `docs/mysql-schema.sql`.

## Project Structure

```text
src/main/java/com/cyclecare
  config/        Security, global model advice, seed data
  controller/    MVC and REST controllers
  domain/        JPA entities and enums
  dto/           Form DTOs with validation
  repository/    Spring Data JPA repositories
  security/      Custom user details service
  service/       Business logic, analytics, reports, assistant
src/main/resources
  static/        CSS and JavaScript
  templates/     Thymeleaf pages and fragments
docs/            Architecture, schema, endpoints, roadmap
```

## Documentation

- `docs/ARCHITECTURE.md`
- `docs/ER_DIAGRAM.md`
- `docs/API_ENDPOINTS.md`
- `docs/IMPLEMENTATION_PLAN.md`
- `docs/ROADMAP.md`
- `docs/mysql-schema.sql`

## Safety Disclaimer

CycleCare is an educational tracking application and is not a medical diagnostic tool. Users should consult qualified medical professionals for diagnosis, treatment, severe symptoms, unusual symptoms, or persistent concerns.
