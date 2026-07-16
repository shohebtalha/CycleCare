from __future__ import annotations

from enum import Enum
from typing import List

import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sklearn.cluster import KMeans
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler

app = FastAPI(title="CycleCare ML Service", version="1.0.0")


class InsightType(str, Enum):
    INFO = "INFO"
    WARNING = "WARNING"
    ALERT = "ALERT"


class FlowEntry(BaseModel):
    date: str
    flow_level: str = Field(alias="flowLevel")


class SleepLog(BaseModel):
    date: str
    hours: float


class WaterLog(BaseModel):
    date: str
    amount: int


class Mood(BaseModel):
    date: str
    type: str
    intensity: int = 3


class Symptom(BaseModel):
    date: str
    type: str
    severity: int = 1


class MlInsightRequest(BaseModel):
    flowEntries: List[FlowEntry] = []
    sleepLogs: List[SleepLog] = []
    waterLogs: List[WaterLog] = []
    moods: List[Mood] = []
    symptoms: List[Symptom] = []


class HealthIntelligenceCard(BaseModel):
    title: str
    message: str
    type: InsightType
    icon: str
    category: str = "ML insight"


class ModelDriver(BaseModel):
    label: str
    score: int
    explanation: str


class MlInsightResponse(BaseModel):
    cards: List[HealthIntelligenceCard]
    adaptiveRiskScore: int
    riskLevel: str
    modelConfidence: int
    primaryDriver: str
    nextBestAction: str
    drivers: List[str]
    driverDetails: List[ModelDriver]


FLOW_WEIGHTS = {
    "SPOTTING": 1,
    "LIGHT": 2,
    "MODERATE": 3,
    "HEAVY": 4,
    "VERY_HEAVY": 5,
}
STRESS_MOODS = {"STRESSED", "ANXIOUS", "IRRITATED", "SAD"}


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/ml/insights", response_model=MlInsightResponse)
def ml_insights(payload: MlInsightRequest) -> MlInsightResponse:
    frame = build_daily_frame(payload)
    cards: list[HealthIntelligenceCard] = []

    if frame.empty or len(frame) < 4:
        cards.append(HealthIntelligenceCard(
            title="Baseline is still learning",
            message="CycleCare needs more overlapping symptom, mood, sleep, water, and flow logs before it can find reliable personal patterns.",
            type=InsightType.INFO,
            icon="brain-circuit",
        ))
        return MlInsightResponse(
            cards=cards,
            adaptiveRiskScore=0,
            riskLevel="Learning",
            modelConfidence=18,
            primaryDriver="CycleCare needs more overlapping daily logs before it can score risk reliably.",
            nextBestAction="Log cycle, flow, symptoms, mood, sleep, and hydration together for several days so CycleCare has enough overlap.",
            drivers=["Baseline learning"],
            driverDetails=[ModelDriver(
                label="Baseline learning",
                score=18,
                explanation="Fewer than four overlapping daily records were available for model scoring.",
            )],
        )

    cards.extend(detect_anomalies(frame))
    cards.extend(detect_sleep_hydration_gaps(frame))
    cards.extend(cluster_symptoms_and_moods(frame))
    cards.extend(detect_lagged_stress_symptom_pattern(frame))
    model_summary = score_model_output(frame)

    if not cards:
        cards.append(HealthIntelligenceCard(
            title="No unusual ML pattern detected",
            message="Your recent 90-day pattern looks close to your current baseline. CycleCare will keep checking for symptom clusters, mood shifts, and wellness changes as new logs arrive.",
            type=InsightType.INFO,
            icon="activity",
        ))

    return MlInsightResponse(cards=cards[:5], **model_summary)


def build_daily_frame(payload: MlInsightRequest) -> pd.DataFrame:
    dates = set()
    for collection in [payload.flowEntries, payload.sleepLogs, payload.waterLogs, payload.moods, payload.symptoms]:
        dates.update(item.date for item in collection)
    if not dates:
        return pd.DataFrame()

    frame = pd.DataFrame({"date": pd.to_datetime(sorted(dates))})
    frame["flow_score"] = 0.0
    frame["sleep_hours"] = 0.0
    frame["water_ml"] = 0.0
    frame["stress_mood"] = 0.0
    frame["mood_intensity"] = 0.0
    frame["symptom_severity"] = 0.0
    frame["symptom_count"] = 0.0

    for entry in payload.flowEntries:
        mask = frame["date"] == pd.to_datetime(entry.date)
        frame.loc[mask, "flow_score"] = max(frame.loc[mask, "flow_score"].max(), FLOW_WEIGHTS.get(entry.flow_level.upper(), 0))

    for log in payload.sleepLogs:
        frame.loc[frame["date"] == pd.to_datetime(log.date), "sleep_hours"] = log.hours

    water_by_date: dict[str, int] = {}
    for log in payload.waterLogs:
        water_by_date[log.date] = water_by_date.get(log.date, 0) + log.amount
    for date, amount in water_by_date.items():
        frame.loc[frame["date"] == pd.to_datetime(date), "water_ml"] = amount

    for mood in payload.moods:
        mask = frame["date"] == pd.to_datetime(mood.date)
        frame.loc[mask, "mood_intensity"] = frame.loc[mask, "mood_intensity"] + mood.intensity
        if mood.type.upper() in STRESS_MOODS:
            frame.loc[mask, "stress_mood"] = frame.loc[mask, "stress_mood"] + mood.intensity

    symptom_groups: dict[str, list[int]] = {}
    for symptom in payload.symptoms:
        symptom_groups.setdefault(symptom.date, []).append(symptom.severity)
    for date, severities in symptom_groups.items():
        mask = frame["date"] == pd.to_datetime(date)
        frame.loc[mask, "symptom_count"] = len(severities)
        frame.loc[mask, "symptom_severity"] = max(severities)

    return frame.sort_values("date").reset_index(drop=True)


def detect_anomalies(frame: pd.DataFrame) -> list[HealthIntelligenceCard]:
    if len(frame) < 8:
        return []

    features = frame[["flow_score", "sleep_hours", "water_ml", "stress_mood", "symptom_severity", "symptom_count"]]
    scaled = StandardScaler().fit_transform(features)
    contamination = min(0.25, max(0.08, 2 / len(frame)))
    labels = IsolationForest(contamination=contamination, random_state=42).fit_predict(scaled)
    anomaly_rows = frame.loc[labels == -1].tail(2)

    cards: list[HealthIntelligenceCard] = []
    for _, row in anomaly_rows.iterrows():
        reasons = []
        if row.sleep_hours and row.sleep_hours < 5.5:
            reasons.append(f"sleep dropped to {row.sleep_hours:.1f} hours")
        if row.symptom_severity >= 4:
            reasons.append(f"symptom severity reached {int(row.symptom_severity)}/5")
        if row.stress_mood >= 4:
            reasons.append("stress-like mood intensity was elevated")
        if row.flow_score >= 4:
            reasons.append("flow was heavy")
        if row.water_ml and row.water_ml < 1200:
            reasons.append(f"hydration was only {int(row.water_ml)} ml")

        if reasons:
            cards.append(HealthIntelligenceCard(
                title="ML anomaly detected",
                message=f"{row.date.date()} looked unusual versus your recent baseline because " + ", ".join(reasons) + ".",
                type=InsightType.ALERT if row.symptom_severity >= 4 or row.sleep_hours < 5 else InsightType.WARNING,
                icon="radar",
                category="Anomaly detection",
            ))
    return cards


def detect_sleep_hydration_gaps(frame: pd.DataFrame) -> list[HealthIntelligenceCard]:
    cards: list[HealthIntelligenceCard] = []
    logged_sleep = frame[frame["sleep_hours"] > 0]
    if not logged_sleep.empty:
        low_sleep_nights = int((logged_sleep["sleep_hours"] < 6).sum())
        average_sleep = logged_sleep["sleep_hours"].mean()
        if low_sleep_nights >= 2 or average_sleep < 6.5:
            cards.append(HealthIntelligenceCard(
                title="Sleep recovery gap",
                message=(
                    f"You logged {low_sleep_nights} night(s) below 6 hours, with an average sleep baseline of {average_sleep:.1f} hours. "
                    "This can contribute to fatigue, headaches, mood changes, and stronger symptoms."
                ),
                type=InsightType.WARNING,
                icon="moon",
                category="Sleep ML",
            ))

    logged_water = frame[frame["water_ml"] > 0]
    if not logged_water.empty:
        low_water_days = int((logged_water["water_ml"] < 1500).sum())
        average_water = logged_water["water_ml"].mean()
        if low_water_days >= 2 or average_water < 1800:
            cards.append(HealthIntelligenceCard(
                title="Hydration gap detected",
                message=(
                    f"You logged {low_water_days} day(s) below 1500 ml, with an average hydration baseline of {average_water:.0f} ml. "
                    "Low hydration can be related to cramps, headaches, fatigue, mood dips, and flow intensity."
                ),
                type=InsightType.WARNING,
                icon="glass-water",
                category="Hydration ML",
            ))

    return cards


def cluster_symptoms_and_moods(frame: pd.DataFrame) -> list[HealthIntelligenceCard]:
    active = frame[(frame["symptom_count"] > 0) | (frame["stress_mood"] > 0) | (frame["flow_score"] > 0)]
    if len(active) < 6:
        return []

    features = active[["flow_score", "stress_mood", "mood_intensity", "symptom_severity", "symptom_count"]]
    scaled = StandardScaler().fit_transform(features)
    labels = KMeans(n_clusters=min(3, len(active)), random_state=42, n_init="auto").fit_predict(scaled)
    active = active.assign(cluster=labels)

    cluster_summary = active.groupby("cluster")[["stress_mood", "symptom_severity", "symptom_count", "flow_score"]].mean()
    candidate = cluster_summary.sort_values(["stress_mood", "symptom_severity"], ascending=False).head(1)
    cluster_id = int(candidate.index[0])
    row = candidate.iloc[0]
    cluster_size = int((active["cluster"] == cluster_id).sum())

    if row.stress_mood < 2 and row.symptom_severity < 3:
        return []

    return [HealthIntelligenceCard(
        title="Symptom and mood cluster found",
        message=(
            f"CycleCare grouped {cluster_size} recent day(s) where stress mood averaged {row.stress_mood:.1f}, "
            f"symptom severity averaged {row.symptom_severity:.1f}/5, and flow score averaged {row.flow_score:.1f}. "
            "This suggests these features may be moving together rather than appearing as isolated logs."
        ),
        type=InsightType.WARNING if row.symptom_severity >= 3.5 else InsightType.INFO,
        icon="network",
        category="Symptom clustering",
    )]


def detect_lagged_stress_symptom_pattern(frame: pd.DataFrame) -> list[HealthIntelligenceCard]:
    if len(frame) < 5:
        return []

    shifted = frame.copy()
    shifted["next_day_symptom"] = shifted["symptom_severity"].shift(-1)
    matches = shifted[(shifted["stress_mood"] >= 4) & (shifted["next_day_symptom"] >= 3)]
    if len(matches) < 2:
        return []

    return [HealthIntelligenceCard(
        title="Stress may precede symptoms",
        message=(
            f"CycleCare found {len(matches)} case(s) where elevated stress-like mood was followed by moderate or severe symptoms within about 24 hours. "
            "This is a correlation, not a diagnosis, but it is useful for planning rest, hydration, and symptom logging."
        ),
        type=InsightType.INFO,
        icon="git-branch",
        category="Cross-feature lag",
    )]


def score_model_output(frame: pd.DataFrame) -> dict:
    drivers = [
        driver(
            "Severe symptom pattern",
            ratio(frame["symptom_severity"] >= 4) * 36,
            "Severe symptom days are raising the risk score.",
        ),
        driver(
            "Heavy flow signal",
            ratio(frame["flow_score"] >= 4) * 24,
            "Heavy or very heavy flow appears in the recent baseline.",
        ),
        driver(
            "Low sleep recovery",
            ratio((frame["sleep_hours"] > 0) & (frame["sleep_hours"] < 6)) * 18,
            "Short sleep is appearing often enough to reduce recovery confidence.",
        ),
        driver(
            "Hydration gap",
            ratio((frame["water_ml"] > 0) & (frame["water_ml"] < 2000)) * 14,
            "Hydration logs are below the 2000 ml target on multiple logged days.",
        ),
        driver(
            "Stress mood cluster",
            ratio(frame["stress_mood"] >= 4) * 14,
            "Stress-like mood intensity is clustering with other wellness signals.",
        ),
        driver(
            "Symptom density",
            ratio(frame["symptom_count"] >= 2) * 12,
            "Multiple symptoms are being logged on the same day.",
        ),
    ]
    drivers = sorted(drivers, key=lambda item: item.score, reverse=True)
    active_drivers = [item for item in drivers if item.score >= 4]
    risk_score = clamp(round(sum(item.score for item in drivers)))
    confidence = clamp(35 + min(45, len(frame) * 4) + min(20, len(active_drivers) * 4))

    primary_driver = (
        active_drivers[0].explanation
        if active_drivers
        else "No dominant ML risk driver was found; recent logs are close to your current baseline."
    )

    return {
        "adaptiveRiskScore": risk_score,
        "riskLevel": risk_level(risk_score),
        "modelConfidence": confidence,
        "primaryDriver": primary_driver,
        "nextBestAction": next_best_action(active_drivers),
        "drivers": [item.label for item in active_drivers[:4]] or ["Baseline learning"],
        "driverDetails": active_drivers[:4] or [driver(
            "Baseline learning",
            20,
            "CycleCare did not find a dominant risk driver in the recent window.",
        )],
    }


def driver(label: str, score: float, explanation: str) -> ModelDriver:
    return ModelDriver(label=label, score=clamp(round(score)), explanation=explanation)


def ratio(mask) -> float:
    total = len(mask)
    if total == 0:
        return 0
    return float(mask.sum()) / total


def risk_level(score: int) -> str:
    if score >= 60:
        return "High attention"
    if score >= 35:
        return "Watch closely"
    return "Stable"


def next_best_action(drivers: list[ModelDriver]) -> str:
    if not drivers:
        return "Keep logging cycle, symptom, mood, water, and sleep data together so CycleCare can continue learning."

    top = drivers[0].label
    if top == "Severe symptom pattern":
        return "Prioritize symptom severity logging and consider clinical guidance if severe symptoms continue."
    if top == "Heavy flow signal":
        return "Track flow, fatigue, and hydration together for the next cycle to separate normal variation from escalation."
    if top == "Low sleep recovery":
        return "Protect sleep around the predicted period window and compare symptom intensity afterward."
    if top == "Hydration gap":
        return "Aim for steadier hydration before and during bleeding days, then compare cramps and headache severity."
    if top == "Stress mood cluster":
        return "Add short journal notes on stressful days so CycleCare can identify possible triggers."
    return "Keep logging cycle, symptom, mood, water, and sleep data together so CycleCare can continue learning."


def clamp(value: int) -> int:
    return max(0, min(100, value))
