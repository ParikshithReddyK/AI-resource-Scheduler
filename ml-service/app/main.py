"""
AI Resource Scheduler - ML Service

Ranks candidate employees for a shift using a model trained on workload
and recency signals. Trains a small model on synthetic data at startup
(no real historical assignment data exists yet) — this gets replaced
with real training data once the app has been used for a while.
"""

import random
from typing import List, Optional

import numpy as np
import pandas as pd
import shap
from fastapi import FastAPI
from pydantic import BaseModel
from sklearn.ensemble import GradientBoostingRegressor

app = FastAPI(
    title="AI Resource Scheduler - ML Service",
    description="Ranks employees for a shift based on workload and recency.",
    version="0.1.0",
)

FEATURE_NAMES = ["workload_count", "days_since_last_assignment"]

model: Optional[GradientBoostingRegressor] = None
explainer: Optional[shap.TreeExplainer] = None


def generate_synthetic_training_data(n: int = 500) -> pd.DataFrame:
    """
    Builds synthetic training examples until real assignment history exists.

    Ground-truth rule being learned: prefer employees with LOWER current
    workload and MORE days since their last assignment (i.e. spread work
    around fairly, don't always pick the same person). Some noise is added
    so the model learns a smooth relationship, not a rigid formula.
    """
    random.seed(42)
    np.random.seed(42)

    workload_count = np.random.randint(0, 16, size=n)
    days_since_last = np.random.randint(0, 61, size=n)

    noise = np.random.normal(0, 5, size=n)
    fitness_score = (
        100
        - (workload_count * 5)
        + (np.minimum(days_since_last, 30) * 1.5)
        + noise
    )
    fitness_score = np.clip(fitness_score, 0, 100)

    return pd.DataFrame(
        {
            "workload_count": workload_count,
            "days_since_last_assignment": days_since_last,
            "fitness_score": fitness_score,
        }
    )


@app.on_event("startup")
def train_model():
    global model, explainer

    df = generate_synthetic_training_data()
    X = df[FEATURE_NAMES]
    y = df["fitness_score"]

    model = GradientBoostingRegressor(
        n_estimators=100, max_depth=3, learning_rate=0.1, random_state=42
    )
    model.fit(X, y)

    explainer = shap.TreeExplainer(model)
    print("ML service: model trained on synthetic data, ready to serve.")


class CandidateFeatures(BaseModel):
    employee_id: str
    workload_count: int
    days_since_last_assignment: int


class RecommendRequest(BaseModel):
    shift_id: str
    candidates: List[CandidateFeatures]


class RankedCandidate(BaseModel):
    employee_id: str
    score: float
    explanation: dict


class RecommendResponse(BaseModel):
    ranked: List[RankedCandidate]


@app.get("/health")
def health():
    return {"status": "UP", "service": "ml-service", "model_ready": model is not None}


@app.post("/recommend", response_model=RecommendResponse)
def recommend(request: RecommendRequest):
    if not request.candidates:
        return RecommendResponse(ranked=[])

    df = pd.DataFrame(
        [
            {
                "workload_count": c.workload_count,
                "days_since_last_assignment": c.days_since_last_assignment,
            }
            for c in request.candidates
        ]
    )

    scores = model.predict(df)
    shap_values = explainer.shap_values(df)

    ranked = []
    for i, candidate in enumerate(request.candidates):
        explanation = {
            FEATURE_NAMES[j]: round(float(shap_values[i][j]), 2)
            for j in range(len(FEATURE_NAMES))
        }
        ranked.append(
            RankedCandidate(
                employee_id=candidate.employee_id,
                score=round(float(scores[i]), 2),
                explanation=explanation,
            )
        )

    ranked.sort(key=lambda r: r.score, reverse=True)
    return RecommendResponse(ranked=ranked)