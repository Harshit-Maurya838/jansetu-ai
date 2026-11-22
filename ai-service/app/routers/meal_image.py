from fastapi import APIRouter, File, HTTPException, UploadFile

from app.schemas.meal import MealImageResult

router = APIRouter(prefix="/meal-image", tags=["meal-image"])

MAX_BYTES = 5 * 1024 * 1024


@router.post("/analyze", response_model=MealImageResult)
async def analyze(image: UploadFile = File(...)) -> MealImageResult:
    if image.content_type not in ("image/jpeg", "image/png"):
        raise HTTPException(status_code=415, detail="only JPEG or PNG")
    data = await image.read(MAX_BYTES + 1)
    if len(data) > MAX_BYTES:
        raise HTTPException(status_code=413, detail="image larger than 5 MB")
    # TODO(team): OpenCV pipeline (docs/ml-overview.md#3-meal-image-quality-analysis):
    # decode -> blur/brightness check -> HSV colour histogram + texture features -> classifier.
    raise HTTPException(status_code=501, detail="model not trained yet")
