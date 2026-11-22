from fastapi import APIRouter, HTTPException

from app.schemas.growth import Measurement, ZScores

router = APIRouter(prefix="/growth", tags=["growth"])


@router.post("/zscores", response_model=ZScores)
def zscores(m: Measurement) -> ZScores:
    # TODO(team): look up L, M, S for (indicator, sex, age_days / length) from the WHO tables and call
    # app.models.zscore.lms_zscore; flag |WAZ|>6, HAZ<-6 or >6, |WHZ|>5 as biologically implausible.
    raise HTTPException(status_code=501, detail="WHO LMS tables not loaded yet")
