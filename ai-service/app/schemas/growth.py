from datetime import date
from enum import Enum

from pydantic import BaseModel, Field

from app.schemas.common import Prediction


class Sex(str, Enum):
    MALE = "MALE"
    FEMALE = "FEMALE"


class Measurement(BaseModel):
    measured_on: date
    age_days: int = Field(ge=0, le=6 * 366)
    sex: Sex
    weight_kg: float | None = Field(default=None, gt=0, lt=60)
    height_cm: float | None = Field(default=None, gt=30, lt=150)
    recumbent: bool = Field(description="True if length was measured lying down (usually < 2 years)")
    oedema: bool = False


class ZScores(BaseModel):
    waz: float | None = None  # weight-for-age
    haz: float | None = None  # height/length-for-age
    whz: float | None = None  # weight-for-height/length
    flags: list[str] = Field(default_factory=list, description="e.g. BIOLOGICALLY_IMPLAUSIBLE_WHZ")


class MalnutritionRequest(BaseModel):
    child_id: str
    history: list[Measurement] = Field(min_length=1, description="oldest first; last item is the current visit")
    birth_weight_kg: float | None = None
    recent_absence_days: int | None = None
    missed_vaccinations: int | None = None


class MalnutritionPrediction(Prediction):
    pass
