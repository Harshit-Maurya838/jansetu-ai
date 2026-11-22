"""JanSetu AI service. Called only by the Spring Boot backend, never directly by the mobile app.

Every prediction is a recommendation for human review (docs/ml-overview.md#how-predictions-are-exposed-and-reviewed).
"""

import os
import secrets

from fastapi import Depends, FastAPI, Header, HTTPException

from app.routers import elderly_risk, growth, malnutrition, meal_image

API_KEY = os.environ.get("AI_SERVICE_API_KEY", "")


def require_api_key(x_api_key: str = Header(default="")) -> None:
    """Shared-secret check between backend and ai-service (service is not exposed publicly)."""
    if not API_KEY or not secrets.compare_digest(x_api_key, API_KEY):
        raise HTTPException(status_code=401, detail="invalid api key")


app = FastAPI(title="JanSetu AI Service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP", "service": "jansetu-ai-service"}


for r in (growth.router, malnutrition.router, meal_image.router, elderly_risk.router):
    app.include_router(r, prefix="/v1", dependencies=[Depends(require_api_key)])
