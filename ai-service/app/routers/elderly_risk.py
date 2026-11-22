from fastapi import APIRouter, HTTPException

from app.schemas.elderly import ElderlyRiskPrediction, ElderlyRiskRequest

router = APIRouter(prefix="/elderly-risk", tags=["elderly-risk"])


@router.post("/predict", response_model=ElderlyRiskPrediction)
def predict(req: ElderlyRiskRequest) -> ElderlyRiskPrediction:
    # TODO(team): rule-based screening first (BP >= 180/120, glucose < 70 or > 300), then the trained model.
    raise HTTPException(status_code=501, detail="model not trained yet")
