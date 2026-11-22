from fastapi import APIRouter, HTTPException

from app.schemas.growth import MalnutritionPrediction, MalnutritionRequest

router = APIRouter(prefix="/malnutrition", tags=["malnutrition"])


@router.post("/predict", response_model=MalnutritionPrediction)
def predict(req: MalnutritionRequest) -> MalnutritionPrediction:
    # TODO(team): load malnutrition_rf_v1.joblib, build features (docs/ml-overview.md#1-malnutrition-risk-prediction).
    raise HTTPException(status_code=501, detail="model not trained yet")
