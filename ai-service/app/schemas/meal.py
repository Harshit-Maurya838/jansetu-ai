from enum import Enum

from pydantic import BaseModel, Field


class MealQualityLabel(str, Enum):
    ACCEPTABLE = "ACCEPTABLE"
    POOR = "POOR"
    UNCERTAIN = "UNCERTAIN"  # blurry/dark photo or low confidence -> always human review


class MealImageResult(BaseModel):
    label: MealQualityLabel
    score: float = Field(ge=0, le=1, description="probability of POOR")
    image_quality_ok: bool
    reasons: list[str] = Field(default_factory=list)
    model_name: str
    model_version: str
    requires_human_review: bool = True
