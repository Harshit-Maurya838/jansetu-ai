from datetime import datetime

from pydantic import BaseModel, Field

from app.schemas.common import Prediction


class VitalReading(BaseModel):
    recorded_at: datetime
    systolic: int | None = Field(default=None, ge=50, le=260)
    diastolic: int | None = Field(default=None, ge=30, le=160)
    pulse: int | None = Field(default=None, ge=20, le=220)
    blood_glucose_mg_dl: int | None = Field(default=None, ge=20, le=700)
    glucose_context: str | None = None


class ElderlyRiskRequest(BaseModel):
    beneficiary_id: str
    age_years: int = Field(ge=40, le=120)
    lives_alone: bool
    vitals: list[VitalReading] = Field(min_length=1)
    missed_medicine_doses_30d: int | None = None
    missed_appointments_90d: int | None = None
    sos_count_90d: int | None = None


class ElderlyRiskPrediction(Prediction):
    pass
