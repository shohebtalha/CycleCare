# API Endpoints

## MVC Routes

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/` | Redirect to login or dashboard |
| GET | `/login` | Login page |
| POST | `/login` | Spring Security login processing |
| GET | `/register` | Registration page |
| POST | `/register` | Create user account |
| GET | `/forgot-password` | Forgot password form |
| POST | `/forgot-password` | Generate reset token |
| GET | `/reset-password` | Reset password page |
| POST | `/reset-password` | Update password |
| POST | `/logout` | Logout |
| GET | `/dashboard` | User dashboard |
| GET | `/profile` | Profile form |
| POST | `/profile` | Update profile |
| GET | `/cycles` | Cycle tracking page |
| POST | `/cycles` | Save cycle entry |
| GET | `/symptoms` | Symptom tracking page |
| POST | `/symptoms` | Save symptom entry |
| GET | `/moods` | Mood tracking page |
| POST | `/moods` | Save mood entry |
| GET | `/wellness` | Nutrition, exercise, water, sleep page |
| POST | `/water` | Save water intake |
| POST | `/sleep` | Save sleep entry |
| GET | `/journal` | Journal page |
| POST | `/journal` | Save journal entry |
| GET | `/calendar` | Calendar view |
| GET | `/analytics` | Analytics charts and alerts |
| GET | `/assistant` | Assistant page |
| POST | `/assistant` | Ask assistant |
| GET | `/reports` | Complete history |
| GET | `/reports/export` | Export PDF report |

## REST Endpoints

All `/api/**` endpoints require authentication.

| Method | Path | Response |
| --- | --- | --- |
| GET | `/api/dashboard` | Dashboard summary JSON |
| GET | `/api/cycle/current` | Current cycle prediction or 204 |
| GET | `/api/analytics` | Cycle, mood, symptom, regularity analytics |
| GET | `/api/calendar?month=YYYY-MM` | Calendar day markers |
| GET | `/api/recommendations` | Current phase nutrition and exercises |
| POST | `/api/water` | Log water intake and return daily total |

Example `POST /api/water` body:

```json
{
  "entryDate": "2026-06-22",
  "amountMl": 250
}
```
