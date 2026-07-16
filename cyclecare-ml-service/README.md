# CycleCare ML Service

FastAPI microservice for CycleCare health intelligence.

## Run locally

Windows:

```bat
start.bat
```

On this machine, `start.bat` prefers Codex's bundled Python 3.12 runtime when present because the system Python 3.14 may not have prebuilt scikit-learn wheels.

Linux/macOS/Git Bash:

```bash
./setup.sh
```

The service runs on port `8000`.

## Endpoints

- `GET /health`
- `POST /api/ml/insights`

The insight endpoint accepts recent flow, sleep, water, mood, and symptom logs and returns cards compatible with the Spring Boot `HealthIntelligenceCard` DTO.

Open `http://localhost:8000/health` in a browser to check whether the service is running. The `/api/ml/insights` endpoint is POST-only, so opening it directly in the browser is not a valid check.

## Spring Boot integration

The Java app reads:

```properties
cyclecare.ml.enabled=true
cyclecare.ml.base-url=http://localhost:8000
```

If the ML service is offline, CycleCare falls back to the existing heuristic health intelligence cards.
