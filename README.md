# 🌸 CycleCare – AI-Powered Menstrual Health & Wellness Platform

> A full-stack Spring Boot application for menstrual health tracking, cycle prediction, wellness monitoring, AI-assisted guidance, and health analytics.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring_Security-Secured-success)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-purple)
![Chart.js](https://img.shields.io/badge/Chart.js-Analytics-red)
![Gemini AI](https://img.shields.io/badge/Google-Gemini_AI-blue)
![License](https://img.shields.io/badge/Status-Portfolio_Project-pink)

---

# 📖 About

CycleCare is a modern **full-stack menstrual health management platform** built using **Java Spring Boot** that enables users to monitor their menstrual cycle, symptoms, moods, nutrition, hydration, sleep, and overall wellness.

The application combines **predictive cycle analytics**, **interactive dashboards**, **AI-powered health guidance**, and **PDF health reports** into a secure, responsive web application.

> **Note:** CycleCare is intended for educational and wellness tracking purposes only. It does **not** provide medical diagnosis.

---

# ✨ Key Highlights

- 🔐 Secure Authentication with Spring Security & BCrypt
- 🌸 Intelligent Menstrual Cycle Prediction
- 🤖 AI Health Assistant powered by Google Gemini
- 📊 Interactive Analytics Dashboard
- 📄 PDF Health Report Generation
- 🍎 Personalized Nutrition Recommendations
- 🏃 Phase-Based Exercise Recommendations
- 📅 Interactive Cycle Calendar
- 🌙 Responsive Light & Dark Mode
- ☁️ Cloud Deployment (Render + Railway)

---

# 🚀 Features

## 🔐 Authentication & Security

- User Registration
- Secure Login
- BCrypt Password Encryption
- Strong Password Policy
- Remember-Me Authentication
- Forgot Password
- Reset Password using Secure Token
- Hashed Reset Tokens at Rest
- CSRF Protection
- Basic Rate Limiting for Sensitive Endpoints
- Session Management
- Spring Security Authorization

---

## 👤 User Profile

Maintain personalized information including

- Name
- Age
- Height
- Weight
- Activity Level

These values are used for personalized wellness recommendations.

---

## 🌸 Cycle Prediction

Automatically calculates

- Current Cycle Day
- Next Period Date
- Ovulation Date
- Fertility Window
- Menstrual Phase

using historical cycle information.

---

## ❤️ Daily Health Tracking

Track daily wellness data including

- Symptoms
- Mood
- Water Intake
- Sleep
- Nutrition
- Personal Journal

---

## 🥗 Personalized Recommendations

CycleCare provides educational recommendations based on the current menstrual phase including

### Nutrition

- Foods to Prefer
- Foods to Avoid
- Iron-rich Diet
- Hydration Suggestions

### Exercise

- Yoga
- Walking
- Cardio
- Strength Training
- Recovery Activities

---

## 📅 Interactive Calendar

Displays

- Logged Periods
- Predicted Periods
- Ovulation
- Fertility Window
- Symptoms

using an intuitive monthly calendar.

---

## 📊 Analytics Dashboard

Interactive Chart.js visualizations

- Cycle Length Trend
- Mood Trend
- Symptom Frequency
- Cycle Regularity
- Health Insights

---

## 🤖 AI Health Assistant

Integrated with **Google Gemini API** for educational guidance.

Capabilities include

- Menstrual Health Information
- Nutrition Guidance
- Exercise Suggestions
- Lifestyle Tips
- Wellness Questions
- Educational Health Advice

All responses include appropriate safety disclaimers.

---

## 🚨 Health Alerts

Automatically detects possible concerns such as

- Irregular Cycles
- Repeated Severe Symptoms
- Long Cycle Gaps

---

## 📄 PDF Health Reports

Generate downloadable reports containing

- Cycle History
- Symptoms
- Mood Logs
- Sleep Summary
- Hydration Summary
- Nutrition Journal
- Wellness Notes
- Analytics Summary

---

# 🛠 Technology Stack

## Backend

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate

---

## Frontend

- Thymeleaf
- HTML5
- CSS3
- JavaScript
- Bootstrap 5
- Chart.js

---

## Database

- MySQL

---

## AI Integration

- Google Gemini API

---

## PDF Generation

- OpenPDF

---

## Build Tool

- Maven

---

# 🏗 Architecture

```
                        Browser
                           │
                           ▼
                  Spring MVC Controllers
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
 Cycle Service      Analytics Service    AI Assistant
        │                  │                  │
        │                  │          Google Gemini API
        │                  │
        └──────────────┬───┘
                       ▼
               Spring Data JPA
                       │
                       ▼
                    MySQL
```

---

# 📂 Project Structure

```
CycleCare
│
├── src
│   ├── main
│   │   ├── java/com/cyclecare
│   │   │   ├── config
│   │   │   ├── controller
│   │   │   ├── domain
│   │   │   ├── dto
│   │   │   ├── repository
│   │   │   ├── security
│   │   │   └── service
│   │   │
│   │   └── resources
│   │       ├── static
│   │       │   ├── css
│   │       │   ├── js
│   │       │   └── images
│   │       │
│   │       ├── templates
│   │       │   ├── fragments
│   │       │   └── *.html
│   │       │
│   │       └── application.properties
│   │
│   └── test
│
├── docs
│   ├── API_ENDPOINTS.md
│   ├── ARCHITECTURE.md
│   ├── ER_DIAGRAM.md
│   ├── IMPLEMENTATION_PLAN.md
│   ├── ROADMAP.md
│   └── mysql-schema.sql
│
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
└── LICENSE (optional)
```


---

# 📚 Documentation

Comprehensive documentation is included in the **docs/** directory.

| Document                       | Description |
|--------------------------------|-------------|
| 📘 docs/ARCHITECTURE.md        | Overall application architecture and module interaction |
| 🗄 docs/ER_DIAGRAM.md          | Database entity relationship diagram |
| 🌐 docs/API_ENDPOINTS.md       | REST API documentation |
| 🛠 docs/IMPLEMENTATION_PLAN.md | Development phases and implementation details |
| 🚀 docs/ROADMAP.md             | Planned future enhancements |
| 🗃 docs/mysql-schema.sql       | Complete MySQL schema |

---

# ⚙ Installation

## Clone Repository

```bash
git clone https://github.com/shohebtalha/CycleCare.git
```

```
cd CycleCare
```

---

## Create Database

```sql
CREATE DATABASE cyclecare;
```
Or

```bash
docker compose up -d
```

---

## Configure Database

Update

```
src/main/resources/application.properties
```

```
spring.datasource.url=jdbc:mysql://localhost:3306/cyclecare

spring.datasource.username=root

spring.datasource.password=your_password
```

---

## Run

```
mvn spring-boot:run
```

Application

```
http://localhost:8080
```

---

# ☁ Deployment

### Backend

Render

### Database

Railway MySQL

### 🌐Live Demo

https://cyclecare-iikb.onrender.com

---

# 📈 Project Statistics

- ✔ 12+ Responsive Web Pages
- ✔ Secure Authentication & Authorization
- ✔ AI-Powered Health Assistant
- ✔ Dynamic PDF Report Generation
- ✔ Interactive Analytics Dashboard
- ✔ Chart.js Visualizations
- ✔ REST API Endpoints
- ✔ Responsive Bootstrap UI
- ✔ Cloud Deployment
- ✔ Complete Technical Documentation

---
# 📷 Screenshots

## 🏠 Dashboard

![Dashboard](docs/images/dashboard.png)

---

## 📊 Analytics

![Analytics](docs/images/analytics.png)

---

## 🤖 AI Assistant

![Assistant](docs/images/assistant.png)

---

## 📄 Reports

![Reports](docs/images/reports.png)

---

# 🔮 Future Enhancements

- Push Notifications
- Mobile Application
- Doctor Dashboard
- Wearable Device Integration
- AI-based Symptom Prediction
- Export Reports to Excel
- Multi-language Support

---

# Production Readiness

CycleCare includes several production-oriented safeguards:

- Flyway database migrations for controlled schema changes.
- Spring Boot Actuator health, readiness, liveness, info, and metrics endpoints.
- Response compression and static resource caching.
- HikariCP tuning for small hosted MySQL deployments.
- Production profile with `ddl-auto=validate`.
- CI workflow that runs `mvn test` on pushes and pull requests.
- Strong password validation requiring at least 8 characters, lowercase, uppercase, number, and special character.
- Rate limiting for login, forgot-password, reset-password, and AI assistant requests.
- Password reset tokens are hashed before storage.
- Forgot-password emails are sent through Gmail SMTP using environment variables.
- `REMEMBER_ME_KEY` must be provided in production.

## Email-Based Forgot Password

CycleCare sends password reset links by email. Reset tokens are generated with secure randomness, stored only as SHA-256 hashes, expire after 30 minutes, and are invalidated immediately after use.

Required environment variables:

- `MAIL_USERNAME`: Gmail address used to send CycleCare emails.
- `MAIL_PASSWORD`: Gmail App Password. Do not use your normal Gmail password.
- `APP_BASE_URL`: Public app URL, for example `https://cyclecare.onrender.com`.

Gmail App Password setup:

1. Enable 2-Step Verification on the Google account.
2. Open Google Account > Security > App passwords.
3. Create an app password for Mail.
4. Use the generated 16-character password as `MAIL_PASSWORD`.

Render deployment:

1. In the Render service dashboard, open Environment.
2. Add `MAIL_USERNAME`, `MAIL_PASSWORD`, and `APP_BASE_URL`.
3. Set `APP_BASE_URL` to the deployed HTTPS URL, not localhost.
4. Redeploy the service after changing environment variables.

The forgot-password screen always shows: "If an account exists for this email, a password reset link has been sent." This avoids exposing whether an email address is registered.

---

# Privacy & Safety

CycleCare handles sensitive wellness data. For a real production launch, the following practices are required:

- Use HTTPS-only deployment.
- Keep database credentials and API keys in environment variables.
- Restrict database access to the application service.
- Define a data retention and deletion policy.
- Add a privacy policy explaining what health data is stored and why.
- Avoid using CycleCare as a diagnostic or treatment tool.
- Monitor failed logins, rate-limit events, and slow requests.

---
# ⚠ Disclaimer

CycleCare is designed for educational and wellness tracking purposes only.

The application **does not diagnose, treat, cure, or prevent medical conditions**. Users experiencing severe or persistent symptoms should seek advice from qualified healthcare professionals.

---

# 👨‍💻 Developer

**Shoheb Mohammad**

B.Tech Computer Science & Engineering

Java • Spring Boot • Spring Security • REST APIs • MySQL • Full Stack Development

---

## ⭐ If you found this project useful, consider giving it a Star!
