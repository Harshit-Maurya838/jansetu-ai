from enum import Enum

from pydantic import BaseModel, Field


class RiskLevel(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class Prediction(BaseModel):
    """Shape shared by every model endpoint. The backend stores it as a risk_alerts row (PENDING_REVIEW)."""

    risk_score: float = Field(ge=0, le=1)
    risk_level: RiskLevel
    model_name: str
    model_version: str
    top_factors: list[str] = Field(default_factory=list, description="plain-language reasons for reviewers")
    requires_human_review: bool = True
