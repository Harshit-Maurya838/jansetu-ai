# Trained models

Model artefacts (`*.joblib`) are **not committed**. Training notebooks live in `../notebooks/`.

| File (planned) | Used by | Spec |
|---|---|---|
| `malnutrition_rf_v1.joblib` | `/v1/malnutrition/predict` | [ml-overview.md](../../../docs/ml-overview.md#1-malnutrition-risk-prediction) |
| `meal_quality_v1.joblib` | `/v1/meal-image/analyze` | [ml-overview.md](../../../docs/ml-overview.md#3-meal-image-quality-analysis) |
| `elderly_risk_lr_v1.joblib` | `/v1/elderly-risk/predict` | [ml-overview.md](../../../docs/ml-overview.md#5-elderly-health-risk) |
| `who_lms/*.csv` | `zscore.py` | WHO Child Growth Standards tables. TODO(team): add |

TODO(team): decide where artefacts are stored (cloud bucket vs. Git LFS) and record each model's version, training data snapshot and metrics in a model card.
